package com.nms.utility;

public class Constant {

    // constant for escape characters

    public static final String COLON = ":";
    public static final String SEMI_COLON = ";";
    public static final String EMPTY_STRING = "";

    // constant for verticles
    public static final String VERTICAL_DEPLOYMENT_SUCCESS = "All verticles deployed successfully";

    // Routes constant for main routes

    public static final String MAIN_CREDENTIAL_ROUTE = "/nms/credentials/*";
    public static final String MAIN_DISCOVERY_ROUTE = "/nms/discovery/*";


    // Routes constant for crud operations
    public static final String CREATE_ROUTE = "/create";
    public static final String READ_CREDENTIAL_ROUTE = "/get/:credentialsId";
    public static final String READ_DISCOVERY_ROUTE = "/get/:discoveryId";
    public static final String READ_ALL_ROUTE = "/getAll";
    public static final String UPDATE_ROUTE = "/update";
    public static final String DELETE_ROUTE = "/delete/:id";

    //Body handler body limit
    public static final long BODY_LIMIT = 209715200;

    // port number for com.nms.api com.nms.service
    public static final int PORT = 8080;

    // server status messages
    public static final String SERVER_LISTEN_SUCCESS = "Server is listening on port ";
    public static final String SERVER_LISTEN_FAILURE = "Server failed to listen on port ";

    // http constants
    public static final String HEADER_CONTENT_TYPE = "content-type";
    public static final String MIME_TYPE_APPLICATION_JSON = "application/json";
    public static final String STATUS = "status";
    public static final String STATUS_CODE = "status.code";
    public static final String STATUS_MESSAGE = "message";
    public static final String STATUS_RESULT = "result";
    public static final String STATUS_ERRORS = "errors";
    public static final String STATUS_FAIL = "fail";
    public static final String STATUS_SUCCESS = "success";
    public static final String ERROR = "error";
    public static final int STATUS_CODE_OK = 200;
    public static final int STATUS_CODE_BAD_REQUEST = 400;
    public static final int STATUS_CODE_INTERNAL_SERVER_ERROR = 500;
    public static final int STATUS_CODE_CONFLICT = 409;
    public static final String STATUS_MESSAGE_INVALID_INPUT = "Invalid Input";
    public static final String STATUS_MESSAGE_OK = "ok";

    // event bus constants

    public static final String DISCOVERY = "discovery";
    public static final String CREDENTIALS = "credentials";
    public static final String CREATE_CREDENTIALS = "create.credentials";
    public static final String READ_CREDENTIALS = "read.credentials";
    public static final String READ_ALL_CREDENTIALS = "readall.credentials";
    public static final String UPDATE_CREDENTIALS = "update.credentials";
    public static final String DELETE_CREDENTIALS = "delete.credentials";
    public static final String CREATE_DISCOVERY = "create.discovery";
    public static final String READ_DISCOVERY = "read.discovery";
    public static final String READ_ALL_DISCOVERY = "readall.discovery";
    public static final String UPDATE_DISCOVERY = "update.discovery";
    public static final String DELETE_DISCOVERY = "delete.discovery";


    // constants for data
    public static final String DATA_ALREADY_EXISTS = " data already exist!";
    public static final String DATA_DOES_NOT_EXIST = " data does not exist!";
    public static final String CREATE_SUCCESS = " creation successful";
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
    public static final String INVALID = "Invalid";

}
