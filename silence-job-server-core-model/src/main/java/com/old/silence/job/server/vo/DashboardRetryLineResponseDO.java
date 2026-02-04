package com.old.silence.job.server.vo;


import java.util.List;


public class DashboardRetryLineResponseDO {

    /**
     * 任务列表
     */
    private List<Task> taskList;

    /**
     * 排名列表
     */
    private List<Rank> rankList;

    /**
     * 折线图列表
     */
    private List<DashboardLineResponseDO> dashboardLineResponseDOList;

    
    public static class Task {
        private String groupName;

        private Integer run;

        private Integer total;

        public String getGroupName() {
            return groupName;
        }

        public void setGroupName(String groupName) {
            this.groupName = groupName;
        }

        public Integer getRun() {
            return run;
        }

        public void setRun(Integer run) {
            this.run = run;
        }

        public Integer getTotal() {
            return total;
        }

        public void setTotal(Integer total) {
            this.total = total;
        }
    }

    
    public static class Rank {
        private String name;

        private String total;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTotal() {
            return total;
        }

        public void setTotal(String total) {
            this.total = total;
        }
    }

    public List<Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
    }

    public List<Rank> getRankList() {
        return rankList;
    }

    public void setRankList(List<Rank> rankList) {
        this.rankList = rankList;
    }

    public List<DashboardLineResponseDO> getDashboardLineResponseDOList() {
        return dashboardLineResponseDOList;
    }

    public void setDashboardLineResponseDOList(List<DashboardLineResponseDO> dashboardLineResponseDOList) {
        this.dashboardLineResponseDOList = dashboardLineResponseDOList;
    }
}
