package com.runafter.wtt;

import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.runafter.wtt.DateTimeUtils.minimumInDate;

/**
 * Created by runaf on 2017-01-16.
 */

public class InOutLogAnalyzer {
    private static final String TAG = "IOLA";
    private final InOutLogRepository inOutLogRepo;
    private final WorkingTimeRepository workingTimeRepo;

    public InOutLogAnalyzer(InOutLogRepository inOutLogRepo, WorkingTimeRepository workingTimeRepo) {
        this.inOutLogRepo = inOutLogRepo;
        this.workingTimeRepo = workingTimeRepo;
    }

    public Collection<WorkingTime> analyze(long from, List<InOutLog> logsDesc) {
        LinkedList<InOutLog> inOutLogsDesc = asLinkedList(logsDesc);   // desc
        if (!inOutLogsDesc.isEmpty())
            inOutLogsDesc.addAll(inOutLogRepo.findDesc(dateOf(timeOfLast(inOutLogsDesc)), from));

        if (inOutLogsDesc.isEmpty())
            return Collections.emptyList();

        List<WorkingTime> workingTimes = workingTimeRepo.find(dateOf(timeOfLast(inOutLogsDesc)), dateOf(timeOfFirst(inOutLogsDesc)));
        return appliedWorkingTimes(workingTimes, inOutLogsDesc);
    }

    private void resetStartEndTime(Collection<WorkingTime> workingTimes) {
        for (WorkingTime workingTime : workingTimes) {
            workingTime.setStart(0);
            workingTime.setEnd(0);
        }
    }

    Collection<WorkingTime> appliedWorkingTimes(List<WorkingTime> workingTimesList, List<InOutLog> inOutLogsDesc) {
        Map<Long, WorkingTime> workingTimes = asMap(workingTimesList);
        resetStartEndTime(workingTimes.values());
        for (InOutLog log : inOutLogsDesc) {
            long time = log.getTime();
            long date = dateOf(time);
            WorkingTime workingTime = workingTimes.get(date);
            if (workingTime == null) {
                workingTime = new WorkingTime();
                workingTime.setDate(date);
                workingTimes.put(date, workingTime);
            }
            if (log.isInLog()) {
                workingTime.setStart(minIfNotZero(workingTime.getStart(), time));
            } else if (log.isOutLog()) {
                workingTime.setEnd(Math.max(workingTime.getEnd(), time));
            } else {
                Log.w(TAG, "Unknown Log Type " + log);
            }
        }
        return workingTimes.values();
    }

    private long minIfNotZero(long v1, long v2) {
        if (v1 == 0)
            return v2;
        if (v2 == 0)
            return v1;
        return Math.min(v1, v2);
    }


    private Map<Long, WorkingTime> asMap(List<WorkingTime> workingTimes) {
        Map<Long, WorkingTime> map = new LinkedHashMap<>();
        for (WorkingTime workingTime : workingTimes)
            map.put(workingTime.getDate(), WorkingTime.copyOf(workingTime));
        return map;
    }


    private WorkingTime apply(@Nullable WorkingTime workingTime, InOutLog log) {
        //TODO
        if (workingTime == null) {
            workingTime = new WorkingTime();
            workingTime.setDate(dateOf(log.getTime()));
        }

        return null;
    }

    private LinkedList<InOutLog> asLinkedList(List<InOutLog> results) {
        LinkedList<InOutLog> list = new LinkedList<>();
        Iterator<InOutLog> iterator = results.iterator();
        while (iterator.hasNext())
            list.add(iterator.next());
        return list;
    }

    private Long timeOfFirst(LinkedList<InOutLog> lnOutLogs) {
        return lnOutLogs.peekFirst().getTime();
    }

    private Long timeOfLast(LinkedList<InOutLog> lnOutLogs) {
        return lnOutLogs.peekLast().getTime();
    }
    private long dateOf(long time) {
        return minimumInDate(time);
    }
}
