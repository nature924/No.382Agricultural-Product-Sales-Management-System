package org.example.springboot.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.annotation.Resource;
import org.example.springboot.common.Result;
import org.example.springboot.entity.Notice;
import org.example.springboot.mapper.NoticeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RequestMapping("/notice")
@RestController
public class NoticeController {
    @Resource
    NoticeMapper noticeMapper;
    public static final Logger LOGGER = LoggerFactory.getLogger(NoticeController.class);

    /**
     * 获取所有文�?
     * @return 文章列表
     */
    @GetMapping
    public Result<?> getAll() {
        QueryWrapper<Notice> queryWrapper = new QueryWrapper<>();
        List<Notice> notices = noticeMapper.selectList(queryWrapper);
        LOGGER.info("notices:"+notices);
        return Result.success(notices);
    }
    @GetMapping("/limit")
    public Result<?> getWithLimit(@RequestParam(defaultValue = "10") Integer count){
        LOGGER.info("limit:"+count);
        QueryWrapper<Notice> queryWrapper = new QueryWrapper<>();
        // 按时间字段由近到远排�?
        queryWrapper.orderByDesc("time");
    Page<Notice> page = new Page<>(1, count);
        IPage<Notice> resultPage = noticeMapper.selectPage(page, queryWrapper);

        List<Notice> notices = resultPage.getRecords();
        LOGGER.info("notices:" + notices);
        return Result.success(notices);

    }
    @GetMapping("/page")
    public Result<?> getNoticesByPage(
            @RequestParam(defaultValue = "") String title, // 查询条件，名�?
            @RequestParam(defaultValue = "") Integer currentPage, // 当前页码
            @RequestParam(defaultValue = "") Integer size// 每页条数

    ) {
    LOGGER.info("title:"+title+" cP"+currentPage+" size"+size);
        LambdaQueryWrapper<Notice> wrappers = Wrappers.lambdaQuery();
        if (StringUtils.isNotBlank(title)) {
            LOGGER.info("isNotBlank");
            wrappers.like(Notice::getTitle, title);
        }

        Page<Notice> resultPage = noticeMapper.selectPage(new Page<>(currentPage, size), wrappers);


        // 将分页结果封装到Result对象中返�?
        return Result.success(resultPage);
    }
    /**
     * 根据ID获取文章
     * @param id 文章ID
     * @return 文章对象
     */
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable int id) {
        Notice notice = noticeMapper.selectById(id);
        LOGGER.info("notices:"+notice);

        return Result.success(notice);
    }

    /**
     * 添加文章
     * @param notice 文章对象
     * @return 添加结果
     */
    @PostMapping
    public Result<?> add(@RequestBody Notice notice) {
        int res = noticeMapper.insert(notice);
        LOGGER.info("NEW notice:"+notice);
        if(res>0){
            return Result.success();
        }else{
            return Result.error("-1","新增失败");
        }
    }

    /**
     * 更新文章
     * @param id 文章ID
     * @param notice 更新后的文章对象
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<?>  update(@PathVariable int id, @RequestBody Notice notice) {
        notice.setId(id);
        LOGGER.info("UPDATE notice:"+notice);
        int res = noticeMapper.updateById(notice);
        if(res>0){
            return Result.success();
        }else{
            return Result.error("-1","修改失败");
        }
    }
    @DeleteMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestParam List<Integer> ids) {
        LOGGER.info("DELETEBATCH notices IDS:"+ids);
        int res = noticeMapper.deleteBatchIds(ids);
        if(res>0){
            return Result.success();
        }else{
            return Result.error("-1","删除失败");
        }

    }
    /**
     * 删除文章
     * @param id 文章ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<?>  deleteById(@PathVariable int id) {
        LOGGER.info("DELETE notices ID:"+id);
        int res = noticeMapper.deleteById(id);
        if(res>0){
            return Result.success();
        }else{
            return Result.error("-1","删除失败");
        }
    }
}
