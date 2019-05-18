package com.l.scheduleserver.enums;

final public class container {

    public static final String ZK_MASTER_PATH = "/server/master";
    public static final String ZK_WORKER_PATH = "/server/worker";
    public static final String ZK_DEAD_PATH = "/server/dead";
    public static final String ZK_LOCAL_PATH = "/server/lock";
    public static final String WHIPPLETREE = "-";
    //
    public static final String GET_SERVERINFO_HEARTBEAT_NAME = "get server info";
    public static final int GET_SERVERINFO_POOLSIZE = 1;
    public static final long GET_SERVERINFO_TIME = 10;

    public static final String ZK_DELETE_TYPE = "delete";
    public static final String ZK_ADD_TYPE = "add";

    public static final String MASTER_PREFIX = "master";
    public static final String WORKER_PREFIX = "worker";


    public static final String SPRITE = "/";

    public static final int PROCESS_MUTEX_TIME = 10;

    public static final String METHODPATH = "/worker/schedule/add";
}
