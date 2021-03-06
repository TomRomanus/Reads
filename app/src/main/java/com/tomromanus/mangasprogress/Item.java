package com.tomromanus.mangasprogress;

public class Item {
    private String title;
    private int amountWatched;
    private boolean finished;

    public Item(String title, int amountWatched, boolean finished) {
        this.title = title;
        this.amountWatched = amountWatched;
        this.finished = finished;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title){
        if(title.isEmpty()) return;
        this.title = title;
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

    public void subtractAmountWatched() {
        amountWatched--;
    }

    public boolean isFinished() {
        return finished;
    }

    public void toggleFinished() {
        finished = !finished;
    }
}
