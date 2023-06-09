package com.nms.lite.utility;

public class Constant
{

    // constant for escape characters

    public static final String COLON = ":";
    public static final String JSON_EXTENSION = ".json";
    public static final String EMPTY_STRING = "";
    public static final String NEW_LINE = "\n";
    public static final String FORWARD_SLASH = "/";
    public static final String EQUAL_TO = "=";
    public static final String PERCENTAGE = "%";
    public static final String NUMERIC_ZERO_IN_STRING = "0";
    public static String CREDENTIAL_COUNTER = "Credential Counter";

    // constant for verticles
    public static final String VERTICAL_DEPLOYMENT_SUCCESS = "All verticles deployed successfully";

    // Routes constant for main routes

    public static final String MAIN_CREDENTIAL_ROUTE = "/nms/credentials/*";
    public static final String MAIN_DISCOVERY_ROUTE = "/nms/discovery/*";
    public static final String MAIN_PROVISION_ROUTE = "/nms/provision/*";


    // Routes constant for crud operations
    public static final String CREATE_ROUTE = "/create";
    public static final String READ_CREDENTIAL_ROUTE = "/get/:credentialsId";
    public static final String READ_DISCOVERY_ROUTE = "/get/:discoveryId";
    public static final String READ_PROVISION_ROUTE = "/get/:provisionId";
    public static final String RUN_PROVISION_ROUTE = "/run/:discoveryId";
    public static final String RUN_DISCOVERY_ROUTE = "/run/:discoveryId";
    public static final String READ_ALL_ROUTE = "/getAll";
    public static final String UPDATE_ROUTE = "/update";
    public static final String DELETE_CREDENTIAL_ROUTE = "/delete/:credentialsId";
    public static final String DELETE_DISCOVERY_ROUTE = "/delete/:discoveryId";
    public static final String DELETE_PROVISION_ROUTE = "/delete/:provisionId";

    //Body handler body limit
    public static final long BODY_LIMIT = 209715200;

    // port number for com.nms.api com.nms.service
    public static final int PORT = 8080;

    // server status messages
    public static final String SERVER_LISTEN_SUCCESS = "Server is listening on port ";
    public static final String SERVER_LISTEN_FAILURE = "Server failed to listen on port ";

    // http constants
    public static final String STATUS_CODE = "status.code";
    public static final String STATUS_MESSAGE = "message";
    public static final String STATUS_RESULT = "result";
    public static final String STATUS_ERRORS = "errors";
    public static final String STATUS_FAIL = "fail";
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_ERROR = "error";
    public static final int STATUS_CODE_OK = 200;
    public static final int STATUS_CODE_BAD_REQUEST = 400;
    public static final int STATUS_CODE_UNAUTHORIZED = 401;
    public static final int STATUS_CODE_INTERNAL_SERVER_ERROR = 500;
    public static final int STATUS_CODE_CONFLICT = 409;
    public static final String STATUS_MESSAGE_INVALID_INPUT = "Invalid Input";

    // event bus constants

    public static final String DISCOVERY = "discovery";
    public static final String CREDENTIALS = "credentials";
    public static final String PROVISION = "provision";
    public static final String CREATE_CREDENTIALS = "create.credentials";
    public static final String CREATE_PROVISION = "create.provision";
    public static final String READ_CREDENTIALS = "read.credentials";
    public static final String READ_PROVISION = "read.provision";
    public static final String READ_ALL_CREDENTIALS = "readall.credentials";
    public static final String READ_ALL_PROVISION = "readall.provision";
    public static final String UPDATE_CREDENTIALS = "update.credentials";
    public static final String DELETE_CREDENTIALS = "delete.credentials";
    public static final String DELETE_PROVISION = "delete.provision";
    public static final String CREATE_DISCOVERY = "create.discovery";
    public static final String RUN_DISCOVERY = "run.discovery";
    public static final String READ_DISCOVERY = "read.discovery";
    public static final String READ_ALL_DISCOVERY = "readall.discovery";
    public static final String UPDATE_DISCOVERY = "update.discovery";
    public static final String DELETE_DISCOVERY = "delete.discovery";


    // constants for data
    public static final String DATA_ALREADY_EXISTS = " data already exist!";
    public static final String DATA_DOES_NOT_EXIST = " data does not exist!";
    public static final String PROFILE_ALREADY_IN_USE = " profile already in use";
    public static final String DEVICE_NOT_DISCOVERED_MESSAGE = " device not discovered";
    public static final String CREATE_SUCCESS = " creation successful";
    public static final String PROVISION_RUN_SUCCESS = " provision run successful";
    public static final String READ_SUCCESS = " read successful";

    // constants for com.nms.database com.nms.service

    public static final String CREDENTIALS_NAME = "credentialsName";
    public static final String DISCOVERY_NAME = "discoveryName";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String IP_ADDRESS = "ip";
    public static final String PORT_NUMBER = "port";
    public static final String CREDENTIALS_ID = "credentialsId";
    public static final String DISCOVERY_ID = "discoveryId";
    public static final String PROVISION_ID = "provisionId";
    public static final String INVALID = "Invalid";
    public static final String STATUS = "status";
    public static final String PROCESS_STATUS = "processStatus";
    public static final String PROCESS_NORMAL = "normal";
    public static final String PROCESS_ABNORMAL = "abnormal";
    public static final String FPING = "fping";
    public static final String COMMAND_COUNT = "-c";
    public static final String COMMAND_SUPPRESSED = "-q";
    public static final String PACKET_COUNT = "3";
    public static final long DISCOVERY_TIMEOUT = 5000;
    public static final long POLLING_TIMEOUT = 5000;
    public static final long PROVISION_DATA_FETCH_INTERVAL = 10000;
    public static final String FAIL_TYPE = "failType";
    public static final String PING = "ping check failed";
    public static final String GO_PLUGIN_EXE_ABSOLUTE_PATH = "/home/yash/NmsGoPlugin";
    public static final String SERVICE = "service";
    public static final String METRIC_GROUP = "metricGroup";
    public static final String DISCOVER = "discover";
    public static final long PING_TIMEOUT = 4000;
    public static final String PROCESS_ABNORMALLY_TERMINATED = "process abnormally terminated";
    public static final String HOSTNAME = "hostname";
    public static final String DISCOVERY_TIMED_OUT = "discovery timed out";
    public static final String PING_CHECK_TIMED_OUT = "ping check timed out";
    public static final String DISCOVERED = "discovered";
    public static final String TYPE = "type";
    public static final String UPDATE_SUCCESS = "updation success";
    public static final String DELETE_SUCCESS = "deletion success";
    public static final long PROCESS_ABNORMAL_TERMINATION_CODE = 137;
    public static final String DISCOVERY_NOT_FOUND = "0";
    public static final String CREDENTIALS_NOT_FOUND = "1";
    public static final String DEVICE_NOT_DISCOVERED = "2";
    public static final String ALREADY_IN_PROVISION_LIST = "3";
    public static final String SCALAR_METRICS = "scalar";
    public static final String TABULAR_METRICS = "tabular";
    public static final String CPU_METRIC = "cpu";
    public static final String DISK_METRIC = "disk";
    public static final String PROCESS_METRIC = "process";
    public static final String SYSTEM_METRIC = "systeminfo";
    public static final String MEMORY_METRIC = "memory";
    public static final String COLLECT = "collect";
    public static final String OUTPUT_PATH = "/home/yash/PollingData";
    public static final String TIMESTAMP = "timestamp";
    public static final String TIME = "time";
    public static final String DATA = "data";
    public static final String DATA_DUMP_SUCCESS = "data dump success";
    public static final String POLL_FAILURE = "Poll Failure ";
    public static final String EMPTY_SPACE = " ";
    public static final String DIRECTORY_CREATION_SUCCESS = "Directory Creation Success";

}
