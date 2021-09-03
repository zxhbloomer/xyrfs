package com.xyrfs.framework.base.controller.v1;

import com.alibaba.fastjson.JSON;
import com.xyrfs.bean.pojo.fs.UploadFileResultPojo;
import com.xyrfs.common.annotations.SysLogAnnotion;
import com.xyrfs.common.constant.FsConstant;
import com.xyrfs.common.exception.BusinessException;
import com.xyrfs.common.properies.FsConfigProperies;
import com.xyrfs.common.utils.bean.BeanUtilsSupport;
import com.xyrfs.common.utils.servlet.ServletUtil;
import com.xyrfs.excel.bean.importconfig.template.ExcelTemplate;
import com.xyrfs.excel.export.ExcelUtil;
import com.xyrfs.excel.upload.FsExcelReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * controller父类
 * 
 * @author zhangxh
 */
@Slf4j
@Component
public class BaseController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FsConfigProperies fsConfigProperies;

    /** 开发者模式，可以跳过验证码 */
    @Value("${fs.security.develop-model}")
    private Boolean developModel;

    /**
     * 通用文件上传
     * @param fileUrl
     * @return
     */
    public <T> T uploadFile(String fileUrl, Class<T> classOfT) throws IllegalAccessException, InstantiationException {
        // 上传的url
        String uploadFileUrl = fsConfigProperies.getFsUrl();
        FileSystemResource resource = new FileSystemResource(fileUrl);
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        param.add("file", resource);
        param.add("appid", fsConfigProperies.getFsAppid());
        param.add("username", fsConfigProperies.getFsUsername());
        param.add("groupid", fsConfigProperies.getFsGroupid());
        /**
         * request 头信息
         */
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(param, headers);
        ResponseEntity<Map> re = restTemplate.exchange(uploadFileUrl, HttpMethod.POST, httpEntity, Map.class);
        if (re.getStatusCode().value() != HttpStatus.OK.value()) {
            throw new BusinessException("错误文件处理失败");
        }
        UploadFileResultPojo uploadFileResultPojo = JSON.parseObject(JSON.toJSONString(re.getBody().get("data")), UploadFileResultPojo.class);
        // 判断文件是否存在
        File file = new File(fileUrl);
        if (file.exists()) {
            if(!file.delete()) {
                throw new RuntimeException("文件删除失败");
            }
        }
        // 复制UploadFileResultPojo类中的属性到，返回的bean中
        T rtnBean = classOfT.newInstance();
        BeanUtilsSupport.copyProperties(uploadFileResultPojo, rtnBean);

        return rtnBean;
    }

    /**
     * 获取excel导入文件，并check是否是excel文件，然后根据模板定义进行导入
     * 如果有错误，则会生成错误excel，供客户下载查看。
     * @param fileUrl
     * @return
     * @throws IOException
     */
    public FsExcelReader downloadExcelAndImportData(String fileUrl, String jsonConfig) throws IOException {
        ExcelTemplate et = initExcelTemplate(jsonConfig);

        // 文件下载到流
        ResponseEntity<byte[]> rtnResponse = restTemplate.getForEntity(fileUrl, byte[].class);
        InputStream is =  new ByteArrayInputStream(rtnResponse.getBody());

        // 文件分析，判断是否是excel文档
        if (FileMagic.valueOf(is) == FileMagic.OLE2){
            // Office 2003 ，xls
        } else if (FileMagic.valueOf(is) == FileMagic.OOXML) {
            // Office 2007 +，xlsx
        } else {
            // 非excel文档，报错
            throw new IllegalArgumentException("导入的文件不是office excel，请选择正确的文件来进行上传");
        }

        // 2、按模版进行读取数据
        FsExcelReader fsExcelReader = new FsExcelReader(is, et);

        return fsExcelReader;
    }

    /**
     * 获取excel模版
     * @param jsonConfig
     * @return
     */
    public ExcelTemplate initExcelTemplate(String jsonConfig){
        // 1、获取模板配置类
        ExcelTemplate et = JSON.parseObject(jsonConfig, ExcelTemplate.class);
        // 初始化
        et.initValidator();
        return et;
    }

    /**
     * 通用文件下载
     * @param filePath
     * @param fileName
     * @param response
     */
    public void fileDownLoad(String filePath, String fileName, HttpServletResponse response) throws IOException {
        ExcelUtil.download(filePath, fileName , response);
    }

    /**
     * 加密密码
     * @param psdOrignalCode
     * @return
     */
    public String getPassword(String psdOrignalCode){
        //加密对象
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodePassword = passwordEncoder.encode(psdOrignalCode);
        return encodePassword;
    }
}
