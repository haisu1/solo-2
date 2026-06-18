package com.office.supplies.query;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryQuery extends BaseQuery {

    private Long parentId;

    public boolean hasParentId() {
        return parentId != null;
    }

    public static CategoryQueryBuilder builder() {
        return new CategoryQueryBuilder();
    }

    public static class CategoryQueryBuilder {
        private Long current = 1L;
        private Long size = 10L;
        private String keyword;
        private Long parentId;

        public CategoryQueryBuilder current(Long current) {
            this.current = current;
            return this;
        }

        public CategoryQueryBuilder size(Long size) {
            this.size = size;
            return this;
        }

        public CategoryQueryBuilder keyword(String keyword) {
            this.keyword = keyword;
            return this;
        }

        public CategoryQueryBuilder parentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public CategoryQuery build() {
            CategoryQuery query = new CategoryQuery();
            query.setCurrent(this.current);
            query.setSize(this.size);
            query.setKeyword(this.keyword);
            query.setParentId(this.parentId);
            return query;
        }
    }
}
