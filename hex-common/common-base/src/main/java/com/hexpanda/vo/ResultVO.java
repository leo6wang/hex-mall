package com.hexpanda.vo;

import com.hexpanda.enums.BusinessEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 项目统一响应结果对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("项目统一响应结果对象")
public class ResultVO<T> implements Serializable {

    @ApiModelProperty("状态码")
    private Integer code = 200;

    @ApiModelProperty("消息")
    private String msg = "ok";

    @ApiModelProperty("数据")
    private T data;

    /**
     * 操作成功
     */
    public static <T> ResultVO<T> success(T data) {
        ResultVO<T> resultVO = new ResultVO<>();
        resultVO.setData(data);
        return resultVO;
    }

    /**
     * 操作失败
     */
    public static <Void> ResultVO<Void> fail(Integer code, String msg) {
        ResultVO<Void> resultVO = new ResultVO<>();
        resultVO.setCode(code);
        resultVO.setMsg(msg);
        resultVO.setData(null);
        return resultVO;
    }

    /**
     * 操作失败
     */
    public static <T> ResultVO<T> fail(BusinessEnum businessEnum) {
        ResultVO<T> resultVO = new ResultVO<>();
        resultVO.setCode(businessEnum.getCode());
        resultVO.setMsg(businessEnum.getDesc());
        resultVO.setData(null);
        return resultVO;
    }

    /**
     * 处理用户的操作
     */
    public static ResultVO<String> handle(Boolean flag) {
        if (flag) {
            return ResultVO.success(null);
        }
        return ResultVO.fail(BusinessEnum.OPERATION_FAIL);
    }
}
