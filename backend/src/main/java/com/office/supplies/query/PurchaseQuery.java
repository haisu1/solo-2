package com.office.supplies.query;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseQuery extends BaseQuery {

    private String status;

    public boolean hasStatus() {
        return status != null && !status.trim().isEmpty();
    }

    public static PurchaseQueryBuilder builder() {
        return new PurchaseQueryBuilder();
    }

    public static class PurchaseQueryBuilder {
        private Long current = 1L;
        private Long size = 10L;
        private String keyword;
        private String status;

        public PurchaseQueryBuilder current(Long current) {
            this.current = current;
            return this;
        }

        public PurchaseQueryBuilder size(Long size) {
            this.size = size;
            return this;
        }

        public PurchaseQueryBuilder keyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public PurchaseQueryBuilder status(String status) {
            this.status = status;
            return this;
        }

        public PurchaseQuery build() {
            PurchaseQuery query = new PurchaseQuery();
            query.setCurrent(this.current);
            query.setSize(this.size);
            query.setKeyword(this.keyword);
            query.setStatus(this.status);
            return query;
        }
    }
}
