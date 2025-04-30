package org.digitalgenesis.account;

import org.digitalgenesis.API;

public class MSAException extends Exception {
    public MSAException(String msaMessage)  {
        API.msaReturnMessage = msaMessage;
    }
}
