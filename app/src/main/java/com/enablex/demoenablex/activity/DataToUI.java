package com.enablex.demoenablex.activity;

public class DataToUI {
    private int id;
    private String data;
    private boolean isReceivedData;
    private boolean receivedStatus;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }


    public boolean isReceivedData() {
        return isReceivedData;
    }

    public void setReceivedData(boolean receivedData) {
        isReceivedData = receivedData;
    }

    public boolean isReceivedStatus() {
        return receivedStatus;
    }

    public void setReceivedStatus(boolean receivedStatus) {
        this.receivedStatus = receivedStatus;
    }

    @Override
    public String toString() {
        return "DataToUI{" +
                "id='" + id + '\'' +
                ", data='" + data + '\'' +
                ", isReceivedData=" + isReceivedData +
                ", receivedStatus=" + receivedStatus +
                '}';
    }
}
