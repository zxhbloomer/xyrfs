package com.xyrfs.bean.pojo.fs;

import com.xyrfs.bean.config.base.v1.BaseVo;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhangxh
 */
@Slf4j
@ToString
@Data
@ApiModel(value = "文件上传结果pojo", description = "文件上传结果pojo")
@EqualsAndHashCode(callSuper=false)
public class UploadFileResultPojo extends BaseVo
{
    /**
     * 文件ID
     */
    private String fileUuid;
    /**
     * 文件名
     */
    private String fileName;
    /**
     * 文件大小(B)
     */
    private Long file_size;
    /**
     * uri Disk
     */
    private String uriDisk;

    /**
     * 除了disk，其他的类型
     * mongodb,qiniu,fastdfs,alioss
     */
    private String fsType;

    /**
     * url fs
     */
    private String uriFs;

    /**
     * 除了disk，其他的类型
     * mongodb,qiniu,fastdfs,alioss所对应的url
     */
    private String fsType2Url;
}
