package com.office.supplies.query;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplyQuery extends BaseQuery {

    private Long categoryId;

    private Integer status;

    private Boolean lowStock;

    public boolean hasCategoryId() {
        return categoryId != null;
    }

    public boolean hasStatus() {
        return status != null;
    }

    public boolean hasLowStock() {
        return lowStock != null && lowStock;
    }

    public static SupplyQueryBuilder builder() {
        return new SupplyQueryBuilder();
    }

    public static class SupplyQueryBuilder {
        private Long current = 1L;
        private Long size = 10L;
        private String keyword;
        private Long categoryId;
        private Integer status;
        private Boolean lowStock;

        public SupplyQueryBuilder current(Long current) {
            this.current = current;
            return this;
        }

        public SupplyQueryBuilder size(Long size) {
            this.size = size;
            return this;
        }

        public SupplyQueryBuilder keyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public SupplyQueryBuilder categoryId(Long categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public SupplyQueryBuilder status(Integer status) {
            this.status = status;
            return this;
        }

        public SupplyQueryBuilder lowStock(Boolean lowStock) {
            this.lowStock = lowStock;
            return this;
        }

        public SupplyQuery build() {
            SupplyQuery query = new SupplyQuery();
            query.setCurrent(this.current);
            query.setSize(this.size);
            query.setKeyword(this.keyword);
            query.setCategoryId(this.categoryId);
            query.setStatus(this.status);
            query.setLowStock(this.lowStock);
            return query;
        }
    }
}
