package com.office.supplies.controller;

import com.office.supplies.common.PageQuery;
import com.office.supplies.common.PageResult;
import com.office.supplies.common.Result;
import com.office.supplies.entity.Category;
import com.office.supplies.entity.Supply;
import com.office.supplies.service.CategoryService;
import com.office.supplies.service.SupplyService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/supplies")
public class SupplyController {

    @Resource
    private SupplyService supplyService;

    @Resource
    private CategoryService categoryService;

    @GetMapping("/categories")
    public Result<List<Category>> getCategoryList() {
        return Result.success(categoryService.getAllCategories());
    }

    @GetMapping("/categories/page")
    public Result<PageResult<Category>> getCategoryPage(@Valid PageQuery query) {
        return Result.success(categoryService.getCategoryPage(query));
    }

    @PostMapping("/categories")
    public Result<Void> createCategory(@RequestBody Category category) {
        categoryService.save(category);
        return Result.successMsg("创建成功");
    }

    @PutMapping("/categories/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        categoryService.updateById(category);
        return Result.successMsg("更新成功");
    }

    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.removeById(id);
        return Result.successMsg("删除成功");
    }

    @GetMapping("/page")
    public Result<PageResult<Supply>> getSupplyPage(
            @Valid PageQuery query,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Boolean lowStock) {
        return Result.success(supplyService.getSupplyPage(query, categoryId, status, lowStock));
    }

    @GetMapping("/list")
    public Result<List<Supply>> getSupplyList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        return Result.success(supplyService.getSupplyList(categoryId, keyword));
    }

    @GetMapping("/{id}")
    public Result<Supply> getSupplyDetail(@PathVariable Long id) {
        return Result.success(supplyService.getSupplyDetail(id));
    }

    @GetMapping("/low-stock")
    public Result<List<Supply>> getLowStockSupplies() {
        return Result.success(supplyService.getLowStockSupplies());
    }

    @PostMapping
    public Result<Void> createSupply(@RequestBody Supply supply) {
        if (supply.getStock() == null) supply.setStock(0);
        if (supply.getMinStock() == null) supply.setMinStock(0);
        if (supply.getMaxStock() == null) supply.setMaxStock(0);
        if (supply.getStatus() == null) supply.setStatus(1);
        supplyService.save(supply);
        return Result.successMsg("创建成功");
    }

    @PutMapping("/{id}")
    public Result<Void> updateSupply(@PathVariable Long id, @RequestBody Supply supply) {
        supply.setId(id);
        supplyService.updateById(supply);
        return Result.successMsg("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteSupply(@PathVariable Long id) {
        supplyService.removeById(id);
        return Result.successMsg("删除成功");
    }

    @PostMapping("/{id}/stock-add")
    public Result<Void> addStock(@PathVariable Long id, @RequestBody java.util.Map<String, Object> params) {
        Integer quantity = params.get("quantity") != null ? Integer.valueOf(params.get("quantity").toString()) : 0;
        String remark = params.get("remark") != null ? params.get("remark").toString() : "";
        try {
            supplyService.addStock(id, quantity, null, remark);
            return Result.successMsg("入库成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
