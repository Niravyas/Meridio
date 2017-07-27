package edu.cmu.meridio;

/**
 * Created by yadav on 7/24/2017.
 */

public class Request {
    private String requestorWantsBook;
    private String acceptorWantsBookID;
    private int id;
    private int fromUserID;
    private String fromUserEmail;
    private int toUserID;
    private String status;

    public Request(int id, int fromUserID,
                   int toUserID,
                   String status,
                   String acceptorWantsBookID,
                   String requestorWantsBook,
                   String fromUserEmail){
        this.acceptorWantsBookID = acceptorWantsBookID;
        this.requestorWantsBook = requestorWantsBook;
        this.fromUserEmail = fromUserEmail;
        this.id = id;
        this.status = status;
        this.toUserID = toUserID;
        this.fromUserID = fromUserID;
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getToUserID() {
        return toUserID;
    }

    public void setToUserID(int toUserID) {
        this.toUserID = toUserID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRequestorWantsBook() {
        return requestorWantsBook;
    }

    public void setRequestorWantsBook(String requestorWantsBook) {
        this.requestorWantsBook = requestorWantsBook;
    }

    public String getAcceptorWantsBookID() {
        return acceptorWantsBookID;
    }

    public void setAcceptorWantsBookID(String acceptorWantsBook) {
        this.acceptorWantsBookID = acceptorWantsBook;
    }

    public int getFromUserID() {
        return fromUserID;
    }

    public void setFromUserID(int fromUserID) {
        this.fromUserID = fromUserID;
    }

    public String getFromUserEmail() {
        return fromUserEmail;
    }

    public void setFromUserEmail(String fromUserEmail) {
        this.fromUserEmail = fromUserEmail;
    }
}
