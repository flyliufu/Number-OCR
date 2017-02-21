package com.lavor.functionsdemo;

import com.lavor.functionsdemo.bean.EnterpriseInfo;
import com.lavor.functionsdemo.bean.JSONEntity;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

/**
 * @author liufu on 2017/2/21.
 */

public interface HttpAPI {

	@GET("enterprise/findEnterpriseInfo")
	Observable<JSONEntity<EnterpriseInfo>> query(@Query("source") String source, @Query("businessLicenseNo") String businessLicenseNo);
}
