package com.mastercard.evaluation.bin.range.events.subscribers;

import com.google.common.eventbus.Subscribe;
import com.mastercard.evaluation.bin.range.events.models.AuditEvent;
import org.springframework.stereotype.Component;

@Component
public class AuditSubscriber implements Subscriber<AuditEvent> {

    /**
     * This event handler is responsible for appending change's to the BinRange's to a file
     * all Additions, Modifications and Deletions to the cache should be logged to the file names "audit-log.txt"
     * which can be found in the root of this project.
     * <p>
     * for each entry updated we should list the previous and current state for that entry to a line in the file.
     * <p>
     * an example line might look like "createdAt=${timestamp}, before=null, after=${new state}" in the case of an addition
     * an example line might look like "createdAt=${timestamp}, before=${old state}, after=null" in the case of a deletion
     * an example line might look like "createdAt=${timestamp}, before=${old state}, after=${new state}" in the case of a modification
     *
     * @param event, The event which contains all update information for a given entry
     */
    @Override
    @Subscribe
    public void handleEvent(AuditEvent event) {
    	
//        TODO - Implement audit log as per Java Doc description
    }
}