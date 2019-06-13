package com.l.scheduleserver.conf.initZookeeper;

import com.l.scheduleserver.masterService.Listener.GetServiceInfoListener;
import com.l.scheduleserver.masterService.Listener.PathCacheListener;
import com.l.scheduleserver.masterService.Listener.SelectLeader;
import com.l.scheduleserver.util.ThreadUtils;
import com.l.scheduleserver.util.serverUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.CreateMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.File;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import static com.l.scheduleserver.enums.container.*;


@Slf4j
@Component
public class ZkClient implements ApplicationRunner {

    private CuratorFramework curatorFramework ;
    private AbstractInitParam abstractInitParam;
    private AtomicInteger atomicInteger = new AtomicInteger();
    @Autowired
    private serverUtil serverUtil;

    /**
     * 初始化连接
     */
    private void init(){
        if(abstractInitParam == null){
            log.info("未配置默认类使用默认属性");
            abstractInitParam = new AbstractInitParam() {
                @Override
                public HashMap<String, Object> getParams(File file, String fileType) {
                    return null;
                }
            };
        }
        //初始化连接
        initConnection();
        //添加状态监听
        addConnectionListener();
        //配置完成连接zookeeper
        start();
        //连接完成后初始化节点
        initMasterPathAndWorkerPath();
        //初始化注册当前服务到zookeeper上
        registerService();
        //初始化service数据
        initServicListeren();
        //初始化定时任务
        initScheduleWork();
        //竞选master
        selectMaster();
        //注册监听节点
        regiestNodePath();
    }

    /**
     * start zookeeper
     */
    public void start(){
        try {
            curatorFramework.start();
            log.info("start zookeeper connection success!");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * close connection
     */
    @PreDestroy
    public void stopCuratorFramework(){
        log.info("停止zookeeperClient");
        curatorFramework.getZookeeperClient().close();
        curatorFramework.close();
    }

    private void initConnection(){
        curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(abstractInitParam.connectString)
                .sessionTimeoutMs(abstractInitParam.sessionTimeOut)
                .connectionTimeoutMs(abstractInitParam.connectionTimeOut)
                .retryPolicy(abstractInitParam.retryPolicy)
                .build();
    }

    /**
     * add listener
     */
    private void addConnectionListener(){
        ConnectionStateListener connectionStateListener = new ConnectionStateListener() {
            public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
                log.info("state changed , current state : " + connectionState.name());
                /**
                 * probably session expired
                 */
                if(connectionState == ConnectionState.LOST){
                    // if lost , then exit
                    // 是否可以自动注册切换
                    log.info("current zookeepr connection state : connection lost ");
                }else if(connectionState == ConnectionState.SUSPENDED){

                }else if(connectionState == ConnectionState.READ_ONLY){

                }else if(connectionState == ConnectionState.CONNECTED){

                }else if(connectionState == ConnectionState.RECONNECTED){

                }
            }
        };

        curatorFramework.getConnectionStateListenable().addListener(connectionStateListener);
    }

    /**
     * 初始化路径
     */
    private void initMasterPathAndWorkerPath(){
        String MasterPath = ZK_MASTER_PATH;
        String WorkerPath = ZK_WORKER_PATH;
        String DeadPath = ZK_DEAD_PATH;

        try {
            if(curatorFramework.checkExists().forPath(MasterPath) == null){
                curatorFramework.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(MasterPath);
            }
            if(curatorFramework.checkExists().forPath(WorkerPath) == null){
                curatorFramework.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(WorkerPath);
            }
            if(curatorFramework.checkExists().forPath(DeadPath) == null){
                curatorFramework.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(DeadPath);
            }
        } catch (Exception e) {
            log.error("初始化路径失败-关闭连接！");
            e.printStackTrace();
            shutdownNow();
        }

    }
    /**
     * 初始化注册当前服务到zookeeper上
     * data appName-address
     */
    public void registerService(){
        try {
            String appName = serverUtil.getApplicationName();
            String address = serverUtil.getHost();
            String workerPath = ZK_WORKER_PATH  + SPRITE + appName;

            while(curatorFramework.checkExists().forPath(workerPath) != null){
                appName = appName + atomicInteger.get();
                workerPath = ZK_WORKER_PATH + SPRITE + appName;
            }
            //注册临时节点，当节点小时则表明应用有错误，则master监听后能做相关操作。
            byte[] data = (appName + WHIPPLETREE + address).getBytes();
            curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(workerPath,data);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (Exception e){
            log.error("初始化注册失败！,原因：{}",e);
        }

    }

    /**
     * 初始化获取worker节点下的server信息
     */
    private void initServicListeren(){

    }

    /**
     * 竞选master
     */
    private void selectMaster(){
        log.info("start run for the master");
        LeaderLatch leaderLatch = new LeaderLatch(curatorFramework, ZK_MASTER_PATH, "client#");
        SelectLeader selectLeader = new SelectLeader(serverUtil.getApplicationName(),ZK_MASTER_PATH,curatorFramework);
        leaderLatch.addListener(selectLeader);
        try {
            leaderLatch.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        selectLeader.start();
    }

    //程序初始化错误，直接停止程序
    private void shutdownNow(){
        log.error("初始化错误，退出程序！");
        stopCuratorFramework();
        System.exit(0);
    }

    /**
     * 将node放在dead目录下，代表当前服务有问题。
     * @param zNode
     * @param serverType
     * @param optionType
     * @throws Exception
     */
    public void handleDeadService(String zNode,String serverType,String optionType) throws Exception {
        String[] zNodes = zNode.split(WHIPPLETREE);
        String ipNode = zNodes[zNode.length() - 1];
        String type = serverType.equals(MASTER_PREFIX) ? MASTER_PREFIX : WORKER_PREFIX;

        if(ZK_ADD_TYPE.equals(optionType)){
            String deletePath = ZK_DELETE_TYPE + SPRITE + type + WHIPPLETREE + ipNode;
            if(curatorFramework.checkExists().forPath(deletePath) == null){
                curatorFramework.create().forPath(type + WHIPPLETREE + ipNode);
            }
        }else if(ZK_DELETE_TYPE.equals(optionType)){
            removeDeadPathNodeByAddress(type,ipNode);
        }
    }

    /**
     * 彻底删除dead下的node
     * @param serverType
     * @param ipNode
     * @throws Exception
     */
    private void removeDeadPathNodeByAddress(String serverType ,String ipNode) throws Exception {
        List<String> deadServers = curatorFramework.getChildren().forPath(ZK_DEAD_PATH);
        for(String server : deadServers){
            if(server.startsWith(serverType + WHIPPLETREE + ipNode))
            curatorFramework.delete().forPath(ZK_DEAD_PATH + serverType + WHIPPLETREE + ipNode);
        }
    }

    /**
     * worker节点监听
     */
    public void regiestNodePath(){
        String workerPath = ZK_WORKER_PATH;
        String localPath = ZK_LOCAL_PATH;

        ThreadFactory threadFactory = ThreadUtils.setThreadFactoryByName("monitor-workerPath-"+workerPath+":");
        PathChildrenCache pathChildrenCache = new PathChildrenCache(curatorFramework,workerPath,true,threadFactory);
        pathChildrenCache.getListenable().addListener(new PathCacheListener(curatorFramework,localPath));
        try {
            pathChildrenCache.start();
            log.info("启动worker目录监听成功！");
        } catch (Exception e) {
            log.error("启动worker目录监听失败！程序退出！");
            shutdownNow();
            e.printStackTrace();
        }
    }

    private void initScheduleWork(){
        ScheduleInit scheduleInit = new ScheduleInit();
        scheduleInit.scanPacketToGetScheduleBean();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        init();
    }
}
