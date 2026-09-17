package com.eims.module.base.controller;

import com.eims.common.exception.BusinessException;
import com.eims.common.result.PageResult;
import com.eims.common.result.Result;
import com.eims.module.base.entity.Customer;
import com.eims.module.base.mapper.CustomerMapper;
import com.eims.module.base.service.BaseDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/base/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerMapper mapper;
    private final BaseDataService baseDataService;

    @GetMapping
    public Result<PageResult<Customer>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(baseDataService.pageCustomers(current, size, keyword));
    }

    @GetMapping("/{id}")
    public Result<Customer> get(@PathVariable Long id) {
        Customer entity = mapper.selectById(id);
        if (entity == null) throw new BusinessException("客户不存在");
        return Result.ok(entity);
    }

    @PostMapping
    public Result<Customer> create(@RequestBody Customer entity) {
        return Result.ok(baseDataService.createCustomer(entity));
    }

    @PutMapping("/{id}")
    public Result<Customer> update(@PathVariable Long id, @RequestBody Customer entity) {
        entity.setId(id);
        mapper.updateById(entity);
        return Result.ok(entity);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        baseDataService.deleteCustomer(id);
        return Result.ok();
    }
}
