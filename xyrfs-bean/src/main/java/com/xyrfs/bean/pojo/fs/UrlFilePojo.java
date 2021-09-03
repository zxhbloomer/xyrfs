package com.xyrfs.bean.pojo.fs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author zxh
 * @date 2019年 07月22日 21:56:31
 */
@Data
@AllArgsConstructor
@Builder
public class UrlFilePojo {
    String remoteFileUrl;
    HttpURLConnection conn;
    URL connUrl;
    String fileName;
    /**
     * 文件后缀 docx
     */
    String fileSuffix;
}
