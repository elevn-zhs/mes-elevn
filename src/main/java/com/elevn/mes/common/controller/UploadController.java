package com.elevn.mes.common.controller;

import com.elevn.mes.common.pojo.Result;
import com.elevn.mes.common.util.DateTools;
import com.elevn.mes.exception.BusinessException;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class UploadController {

    public UploadController(){
        System.out.println("UploadController=========================");
    }

    @Value("${uploadPath}") // 将applicaiont.yaml中的uploadPath属性值注入进来
    private String uploadPath;
    @Value("${imageServerBaseUrl}")
    private String baseUrl;
    @PostMapping("/image")
    public Result<String> uploadImage(@Param("imageFile") MultipartFile imageFile){
        // 获取图片的物理名称
        String originalFileName = imageFile.getOriginalFilename();
        // 需要后缀
        String ext = originalFileName.substring(originalFileName.lastIndexOf("."));
        // 重新生成物理名称
        String fileName = UUID.randomUUID().toString().replace("-","") + "_"
                + DateTools.getDateStr("yyyyMMdd") +"_" + (new Random().nextInt(90) + 10) + ext;
        // 转存文件
        try {
            imageFile.transferTo(new File(uploadPath +  fileName));
            // 响应客户端
            Result<String> result = Result.success(originalFileName);
            result.setData(baseUrl + fileName);
            return result; // 返回的对象中的data就是图片的访问地址
        } catch (IOException e) {
            throw new BusinessException("文件上传失败:" + e.getMessage());
        }
    }
}
