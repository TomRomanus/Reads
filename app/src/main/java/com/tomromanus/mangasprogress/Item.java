package com.tomromanus.mangasprogress;

public class Item {
    private final String title;
    private int amountWatched;
    private boolean finished;
    private String type;

    public Item(String title, int amountWatched, boolean finished, String type) {
        this.title = title;
        this.amountWatched = amountWatched;
        this.finished = finished;
        this.type = type.toUpperCase();
    }

    public String getTitle() {
        return title;
    }

    public int getAmountWatched() {
        return amountWatched;
    }

    public void resetAmountWatched() {
        amountWatched = 0;
    }

    public void addAmountWatched() {
        amountWatched++;
    }

    public void substractAmountWatched() {
        amountWatched--;
    }

    public boolean isFinished() {
        return finished;
    }

    public void toggleFinished() {
        finished = !finished;
    }

    public String getType() {
        return type;
    }

    public void changeType() {
        if(type.equals("M"))
            type = "A";
        else if(type.equals("A"))
            type = "M";
    }
}
