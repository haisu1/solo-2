package com.office.supplies.query;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryCheckQuery extends BaseQuery {

    private String status;

    public boolean hasStatus() {
        return status != null && !status.trim().isEmpty();
    }

    public static InventoryCheckQueryBuilder builder() {
        return new InventoryCheckQueryBuilder();
    }

    public static class InventoryCheckQueryBuilder {
        private Long current = 1L;
        private Long size = 10L;
        private String keyword;
        private String status;

        public InventoryCheckQueryBuilder current(Long current) {
            this.current = current;
            return this;
        }

        public InventoryCheckQueryBuilder size(Long size) {
            this.size = size;
            return this;
        }

        public InventoryCheckQueryBuilder keyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public InventoryCheckQueryBuilder status(String status) {
            this.status = status;
            return this;
        }

        public InventoryCheckQuery build() {
            InventoryCheckQuery query = new InventoryCheckQuery();
            query.setCurrent(this.current);
            query.setSize(this.size);
            query.setKeyword(this.keyword);
            query.setStatus(this.status);
            return query;
        }
    }
}
