package com.eims.module.wms.controller;

import com.eims.common.exception.BusinessException;
import com.eims.common.result.PageResult;
import com.eims.common.result.Result;
import com.eims.module.wms.dto.StocktakeItemRequest;
import com.eims.module.wms.entity.Stocktake;
import com.eims.module.wms.mapper.StocktakeMapper;
import com.eims.module.wms.service.WmsService;
import com.eims.module.wms.vo.StocktakeDetailVO;
import com.eims.module.wms.vo.StocktakeItemVO;
import com.eims.module.wms.vo.StocktakeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wms/stocktake")
@RequiredArgsConstructor
public class StocktakeController {

    private final StocktakeMapper mapper;
    private final WmsService wmsService;

    @GetMapping
    public Result<PageResult<StocktakeVO>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(wmsService.pageStocktake(current, size, keyword));
    }

    @DeleteMapping("/items/{itemId:\\d+}")
    public Result<Void> deleteItem(@PathVariable Long itemId) {
        wmsService.deleteStocktakeItem(itemId);
        return Result.ok();
    }

    @GetMapping("/{id:\\d+}/detail")
    public Result<StocktakeDetailVO> detail(@PathVariable Long id) {
        return Result.ok(wmsService.getStocktakeDetail(id));
    }

    @PostMapping("/{id:\\d+}/init-items")
    public Result<Integer> initItems(@PathVariable Long id) {
        return Result.ok(wmsService.initStocktakeItems(id));
    }

    @PostMapping("/{id:\\d+}/items")
    public Result<StocktakeItemVO> saveItem(@PathVariable Long id, @RequestBody StocktakeItemRequest request) {
        return Result.ok(wmsService.saveStocktakeItem(id, request));
    }

    @PostMapping("/{id:\\d+}/complete")
    public Result<Stocktake> complete(@PathVariable Long id) {
        return Result.ok(wmsService.completeStocktake(id));
    }

    @PostMapping("/{id:\\d+}/confirm")
    public Result<Stocktake> confirm(@PathVariable Long id) {
        return Result.ok(wmsService.confirmStocktake(id));
    }

    @GetMapping("/{id:\\d+}")
    public Result<Stocktake> get(@PathVariable Long id) {
        Stocktake entity = mapper.selectById(id);
        if (entity == null) throw new BusinessException("盘点不存在");
        return Result.ok(entity);
    }

    @PostMapping
    public Result<Stocktake> create(@RequestBody Stocktake entity) {
        return Result.ok(wmsService.createStocktake(entity));
    }

    @PutMapping("/{id:\\d+}")
    public Result<Stocktake> update(@PathVariable Long id, @RequestBody Stocktake entity) {
        Stocktake existing = mapper.selectById(id);
        if (existing == null) throw new BusinessException("盘点不存在");
        if (existing.getStatus() != null && existing.getStatus() >= 2) {
            throw new BusinessException("盘点单已确认，不能再修改");
        }
        entity.setId(id);
        mapper.updateById(entity);
        return Result.ok(entity);
    }

    @DeleteMapping("/{id:\\d+}")
    public Result<Void> delete(@PathVariable Long id) {
        Stocktake existing = mapper.selectById(id);
        if (existing != null && existing.getStatus() != null && existing.getStatus() >= 2) {
            throw new BusinessException("已确认的盘点单不可删除");
        }
        mapper.deleteById(id);
        return Result.ok();
    }
}
