package net.mzi.trackengine.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Poonam on 6/14/2017.
 */
@IgnoreExtraProperties
public class FirebaseTicketData {

    private String Action;

    public void FirebaseTicketData(String Action){

        this.Action=Action;
    }

    public String getAction() {
        return Action;
    }

    @Override
    public String toString() {
        return "value = {Action="+Action+"};";
    }

}
