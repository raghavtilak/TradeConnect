package com.raghav.digitalpaymentsbook.data.network

import com.raghav.digitalpaymentsbook.data.model.*
import com.raghav.digitalpaymentsbook.data.model.apis.CreateConnection
import com.raghav.digitalpaymentsbook.data.model.apis.RetailerProduct
import com.raghav.digitalpaymentsbook.data.model.apis.RetailerSignIn
import com.raghav.digitalpaymentsbook.data.model.apis.ServerResponse
import com.raghav.digitalpaymentsbook.data.model.enums.AnalyticsType
import com.raghav.digitalpaymentsbook.data.model.enums.BusinessTypes
import com.raghav.digitalpaymentsbook.data.model.enums.ConnectionStatus
import com.raghav.digitalpaymentsbook.data.model.enums.OrderStatus
import okhttp3.RequestBody
import org.bson.types.ObjectId
import retrofit2.Response
import retrofit2.http.*

interface UserAPI {

//    @FormUrlEncoded
    @GET("get_user")
    suspend fun getUser( @Query("email") email : String?, @Query("phone") phone : String?,):Response<User>


    @POST("retailer/signin")
    suspend fun retailerSignIn(@Body rsign:RetailerSignIn):Response<String>

    @POST("retailer/signup")
    suspend fun createRetailer(@Body retailer: User):Response<String>


    @POST("customer/signup")
    suspend fun createCustomer(@Body customer: User):Response<String>


    @GET("customer/my_retailers")
    suspend fun getRetailerOfACustomers(@Query("customerEmail") email: String):Response<List<Retailer>>

    @GET("customer/my_transactions")
    suspend fun getTransactionsOfCustomer():Response<List<SellItem>>



    @GET("retailer/customers/{retailerId}")
    suspend fun getCustomersOfARetailer(@Path("retailerId") retailerId: String):Response<List<Customer>>

    @POST("retailer/create_transaction/{retailerId}")
    suspend fun addTransaction(@Body transaction: Transaction,@Path("retailerId")retailerId:String):Response<Transaction>

    @GET("retailer/customer/transactions/{retailerId}")
    suspend fun getAllTransactions(@Path("retailerId")retailerId:String,@Query("customerPhone") custPhone:Long):Response<List<Transaction>>

    @GET("business_types")
    suspend fun getAllBusinessTypes():Response<List<BusinessTypes>>

    @GET("retailer/my_connections")
    suspend fun getMyConnections(@Query("status") status: ConnectionStatus):Response<List<Connection>>

    @GET("retailer/search_retailers")
    suspend fun getSearchedRetailers(@Query("advance_query") advance_query:String):Response<List<Retailer>>

    @POST("retailer/create_connection_req")
    suspend fun createConnectionRequest(@Body connection:CreateConnection):Response<ServerResponse>

    @PUT("retailer/update_connection_req/{id}")
    suspend fun updateConnectionReq(@Path("id") id:ObjectId,@Body body : RequestBody):Response<ServerResponse>

    @PUT("retailer/{id}/update_sale")
    suspend fun updateSell(@Path("id") id:ObjectId,@Body body : RequestBody):Response<ServerResponse>


    @GET("retailer/my_sells")
    suspend fun mySells():Response<List<SellItem>>

    @GET("retailer/my_orders")
    suspend fun myOrders(@Query("status") status:OrderStatus):Response<List<Order>>

    @PUT("retailer/update_order/{id}")
    suspend fun updateOrder(@Path("id") id:ObjectId, @Body body : RequestBody ):Response<ServerResponse>

    @GET("retailer/my_inventory")
    suspend fun myStore():Response<List<StoreItem>>

    @GET("retailer/get_batches_by_id")
    suspend fun getBatchesById(@Query("ids") ids:List<ObjectId>):Response<List<Batch>>

    @GET("retailer/{id}/products")
    suspend fun getRetailerProducts(@Path("id") id:ObjectId):Response<List<RetailerProduct>>

    @GET("retailer/{batchNo}/get_batch_by_no")
    suspend fun findBatch(@Path("batchNo") batchNo:String):Response<Batch>

    @PUT("retailer/update_token")
    suspend fun updateNotificationToken(@Body body: RequestBody):Response<ServerResponse>


    @PUT("retailer/update_batch")
    suspend fun updateBatch(@Query("batchId") batchId : ObjectId,@Body batchDetails: RequestBody):Response<ServerResponse>


    @POST("retailer/add_batch_to_inventory")
    suspend fun addBatchToInventory(@Body batch: RequestBody):Response<ServerResponse>

    @POST("retailer/add_batches_in_bulk")
    suspend fun addBatchesInBulk(@Body body: RequestBody):Response<ServerResponse>

    @POST("retailer/create_order")
    suspend fun createOrder(@Body body: RequestBody):Response<ServerResponse>

    @POST("retailer/record_a_sell")
    suspend fun createSell(@Body body: RequestBody):Response<ServerResponse>


    @GET("retailer/my_products")
    suspend fun getMyProducts():Response<List<RetailerProduct>>

    @GET("retailer/my_profile")
    suspend fun getMyProfile():Response<MyProfile>


    @GET("retailer/my_orders_analytics")
    suspend fun getOrderAnalytics(@Query("startingFrom") startingFrom:String,
                                  @Query("type") type:AnalyticsType,
                                  @Query("noOfDays") noOfDays:Int,
                                  @Query("isCreatedByUser") isCreatedByUser:Boolean):Response<MutableList<AnalyticsData>>
    @GET("retailer/my_sales_analytics")
    suspend fun getSalesAnalytics(@Query("startingFrom") startingFrom:String,
                                  @Query("type") type:AnalyticsType,
                                  @Query("noOfDays") noOfDays:Int,
                                  @Query("isCreatedByUser") isCreatedByUser:Boolean):Response<MutableList<AnalyticsData>>

}