package com.office.supplies.query;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequisitionQuery extends BaseQuery {

    private Long userId;

    private String status;

    private Long departmentId;

    private String startDate;

    private String endDate;

    public boolean hasStatus() {
        return status != null && !status.trim().isEmpty();
    }

    public boolean hasUserId() {
        return userId != null;
    }

    public boolean hasDepartmentId() {
        return departmentId != null;
    }

    public boolean hasDateRange() {
        return (startDate != null && !startDate.trim().isEmpty())
                || (endDate != null && !endDate.trim().isEmpty());
    }

    public static RequisitionQueryBuilder builder() {
        return new RequisitionQueryBuilder();
    }

    public static class RequisitionQueryBuilder {
        private Long userId;
        private String status;
        private Long departmentId;
        private String startDate;
        private String endDate;
        private Long current = 1L;
        private Long size = 10L;
        private String keyword;

        public RequisitionQueryBuilder current(Long current) {
            this.current = current;
            return this;
        }

        public RequisitionQueryBuilder size(Long size) {
            this.size = size;
            return this;
        }

        public RequisitionQueryBuilder keyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public RequisitionQueryBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public RequisitionQueryBuilder status(String status) {
            this.status = status;
            return this;
        }

        public RequisitionQueryBuilder departmentId(Long departmentId) {
            this.departmentId = departmentId;
            return this;
        }

        public RequisitionQueryBuilder startDate(String startDate) {
            this.startDate = startDate;
            return this;
        }

        public RequisitionQueryBuilder endDate(String endDate) {
            this.endDate = endDate;
            return this;
        }

        public RequisitionQuery build() {
            RequisitionQuery query = new RequisitionQuery();
            query.setCurrent(this.current);
            query.setSize(this.size);
            query.setKeyword(this.keyword);
            query.setUserId(this.userId);
            query.setStatus(this.status);
            query.setDepartmentId(this.departmentId);
            query.setStartDate(this.startDate);
            query.setEndDate(this.endDate);
            return query;
        }
    }
}
