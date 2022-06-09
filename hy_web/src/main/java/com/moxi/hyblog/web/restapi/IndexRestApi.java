package com.moxi.hyblog.web.restapi;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.moxi.hyblog.base.enums.EBehavior;
import com.moxi.hyblog.base.enums.EPublish;
import com.moxi.hyblog.base.enums.EStatus;
import com.moxi.hyblog.commons.entity.Blog;
import com.moxi.hyblog.commons.entity.BlogSort;
import com.moxi.hyblog.commons.entity.Tag;
import com.moxi.hyblog.utils.ResultUtil;
import com.moxi.hyblog.utils.StringUtils;
import com.moxi.hyblog.web.annotion.AuthorityVerify.AuthorityVerify;
import com.moxi.hyblog.web.global.MessageConf;
import com.moxi.hyblog.web.global.SysConf;
import com.moxi.hyblog.web.log.BussinessLog;
import com.moxi.hyblog.web.annotion.requestLimit.RequestLimit;
import com.moxi.hyblog.xo.global.SQLConf;
import com.moxi.hyblog.xo.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * <p>
 * 首页 RestApi
 * </p>
 *
 * @author xuzhixiang
 * @since 2018-09-04
 */
@RestController
@RequestMapping("/index")
@Api(value = "首页相关接口", tags = {"首页相关接口"})
@Slf4j
public class IndexRestApi {

    @Autowired
    TagService tagService;

    @Autowired
    LinkService linkService;

    @Autowired
    BlogSortService blogSortService;

    @Autowired
    WebConfigService webConfigService;
    @Autowired
    SysParamsService sysParamsService;
    @Autowired
    BlogService blogService;



    @ApiOperation(value = "按时间戳获取博客", notes = "按时间戳获取博客")
    @GetMapping("/getBlogByTime")
    public String getBlogByTime(HttpServletRequest request,
                                @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                                @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {

        return ResultUtil.result(SysConf.SUCCESS, blogService.getBlogByTime(currentPage, pageSize));
    }


    @ApiOperation(value = "获取文章,标签,分类数量", notes = "获取文章,标签,分类数量", response = String.class)
    @GetMapping("/getCollect")
    public String getCollect() {
        List<Integer> collect = blogService.getCollect();
        Map<String,Integer> map=new HashMap<>();
        map.put("blogCount",collect.get(0));
        map.put("tagCount",collect.get(1));
        map.put("sortCount",collect.get(2));
        return ResultUtil.result(SysConf.SUCCESS,map);

    }

    @RequestLimit(amount = 200, time = 60000)
    @ApiOperation(value = "通过推荐等级获取博客列表", notes = "通过推荐等级获取博客列表")
    @GetMapping("/getBlogByLevel")
    public String getBlogByLevel(HttpServletRequest request,
                                 @ApiParam(name = "level", value = "推荐等级", required = false) @RequestParam(name = "level", required = false, defaultValue = "0") Integer level,
                                 @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                                 @ApiParam(name = "useSort", value = "使用排序", required = false) @RequestParam(name = "useSort", required = false, defaultValue = "0") Integer useSort) {

        return ResultUtil.result(SysConf.SUCCESS, blogService.getBlogPageByLevel(level, currentPage, useSort));
    }

    @ApiOperation(value = "获取首页排行博客", notes = "获取首页排行博客")
    @GetMapping("/getHotBlog")
    public String getHotBlog() {

        log.info("获取首页排行博客");
        return ResultUtil.result(SysConf.SUCCESS, blogService.getHotBlog());
    }

    @ApiOperation(value = "获取首页最新的博客", notes = "获取首页最新的博客")
    @GetMapping("/getNewBlog")
    public String getNewBlog(HttpServletRequest request,
                             @ApiParam(name = "currentPage", value = "当前页数", required = false) @RequestParam(name = "currentPage", required = false, defaultValue = "1") Long currentPage,
                             @ApiParam(name = "pageSize", value = "每页显示数目", required = false) @RequestParam(name = "pageSize", required = false, defaultValue = "10") Long pageSize) {

        log.info("获取首页最新的博客");
        return ResultUtil.result(SysConf.SUCCESS, blogService.getNewBlog(currentPage, null));
    }




    @ApiOperation(value = "获取最热标签", notes = "获取最热标签")
    @GetMapping("/getHotTag")
    public String getHotTag() {
        log.info("获取最热标签");
        String hotTagCount = sysParamsService.getSysParamsValueByKey(SysConf.HOT_TAG_COUNT);
        if(StringUtils.isEmpty(hotTagCount)) {
            log.error("参数配置有误，需重新配置！");
            return ResultUtil.result(SysConf.ERROR, MessageConf.SYSTEM_PARAMS_NOT_FOUNT);
        }
        return ResultUtil.result(SysConf.SUCCESS, tagService.getHotTag(Integer.valueOf(hotTagCount)));
    }

    @ApiOperation(value = "获取友情链接", notes = "获取友情链接")
    @GetMapping("/getLink")
    public String getLink() {

        log.info("获取友情链接");
        String friendlyLinkCount = sysParamsService.getSysParamsValueByKey(SysConf.FRIENDLY_LINK_COUNT);
        if(StringUtils.isEmpty(friendlyLinkCount)) {
            log.error("参数配置有误，需重新配置！");
            return ResultUtil.result(SysConf.ERROR, MessageConf.SYSTEM_PARAMS_NOT_FOUNT);
        }
        return ResultUtil.result(SysConf.SUCCESS, linkService.getListByPageSize(Integer.valueOf(friendlyLinkCount)));
    }

    @BussinessLog(value = "点击友情链接", behavior = EBehavior.FRIENDSHIP_LINK)
    @ApiOperation(value = "增加友情链接点击数", notes = "增加友情链接点击数")
    @GetMapping("/addLinkCount")
    public String addLinkCount(@ApiParam(name = "uid", value = "友情链接UID", required = false) @RequestParam(name = "uid", required = false) String uid) {

        log.info("点击友链");
        return linkService.addLinkCount(uid);
    }

    @ApiOperation(value = "获取网站配置", notes = "获取友情链接")
    @GetMapping("/getWebConfig")
    public String getWebConfig() {

        log.info("获取网站配置");
        return ResultUtil.result(SysConf.SUCCESS, webConfigService.getWebConfigByShowList());
    }

    @BussinessLog(value = "记录访问页面", behavior = EBehavior.VISIT_PAGE)
    @ApiOperation(value = "记录访问页面", notes = "记录访问页面")
    @GetMapping("/recorderVisitPage")
    public String recorderVisitPage(@ApiParam(name = "pageName", value = "页面名称", required = false) @RequestParam(name = "pageName", required = true) String pageName) {

        if (StringUtils.isEmpty(pageName)) {
            return ResultUtil.result(SysConf.SUCCESS, MessageConf.PARAM_INCORRECT);
        }
        return ResultUtil.result(SysConf.SUCCESS, MessageConf.INSERT_SUCCESS);
    }
}

