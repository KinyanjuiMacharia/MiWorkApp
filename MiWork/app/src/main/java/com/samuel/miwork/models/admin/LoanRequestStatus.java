package com.samuel.miwork.models.admin;

/**
 * @author M.Allaudin
 *         <p>Created on 2017-09-29.</p>
 */

public final class LoanRequestStatus {
    public static final int ACCEPTED = 1;
    public static final int REJECTED = 0;
    public static final int NOT_VIEWED = -1;

    public static boolean isAccepted(int status){
        return status == ACCEPTED;
    }

    public static boolean isViewed(int status){
        return status != NOT_VIEWED;
    }
} // LoanRequestStatus
