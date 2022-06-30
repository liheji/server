package top.liheji.server.pojo.other;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author : Galaxy
 * @time : 2022/5/10 0:17
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 发票验证
 */
@NoArgsConstructor
@Data
public class InvoiceVerification {

    /**
     * invoiceDate
     */
    @JSONField(name = "InvoiceDate")
    private String invoiceDate;
    /**
     * wordsResultNum
     */
    @JSONField(name = "words_result_num")
    private Integer wordsResultNum;
    /**
     * machineCode
     */
    @JSONField(name = "MachineCode")
    private String machineCode;
    /**
     * checkCode
     */
    @JSONField(name = "CheckCode")
    private String checkCode;
    /**
     * verifyResult
     */
    @JSONField(name = "VerifyResult")
    private String verifyResult;
    /**
     * invoiceCode
     */
    @JSONField(name = "InvoiceCode")
    private String invoiceCode;
    /**
     * verifyFrequency
     */
    @JSONField(name = "VerifyFrequency")
    private String verifyFrequency;
    /**
     * invoiceType
     */
    @JSONField(name = "InvoiceType")
    private String invoiceType;
    /**
     * invalidSign
     */
    @JSONField(name = "InvalidSign")
    private String invalidSign;
    /**
     * verifyMessage
     */
    @JSONField(name = "VerifyMessage")
    private String verifyMessage;
    /**
     * invoiceNum
     */
    @JSONField(name = "InvoiceNum")
    private String invoiceNum;
    /**
     * wordsResult
     */
    @JSONField(name = "words_result")
    private WordsResult wordsResult;
    /**
     * logId
     */
    @JSONField(name = "log_id")
    private Long logId;

    /**
     * WordsResult
     */
    @NoArgsConstructor
    @Data
    public static class WordsResult {
        /**
         * purchaserName
         */
        @JSONField(name = "PurchaserName")
        private String purchaserName;
        /**
         * purchaserRegisterNum
         */
        @JSONField(name = "PurchaserRegisterNum")
        private String purchaserRegisterNum;
        /**
         * purchaserAddress
         */
        @JSONField(name = "PurchaserAddress")
        private String purchaserAddress;
        /**
         * purchaserBank
         */
        @JSONField(name = "PurchaserBank")
        private String purchaserBank;
        /**
         * sellerName
         */
        @JSONField(name = "SellerName")
        private String sellerName;
        /**
         * sellerRegisterNum
         */
        @JSONField(name = "SellerRegisterNum")
        private String sellerRegisterNum;
        /**
         * sellerAddress
         */
        @JSONField(name = "SellerAddress")
        private String sellerAddress;
        /**
         * sellerBank
         */
        @JSONField(name = "SellerBank")
        private String sellerBank;
        /**
         * totalAmount
         */
        @JSONField(name = "TotalAmount")
        private String totalAmount;
        /**
         * totalTax
         */
        @JSONField(name = "TotalTax")
        private String totalTax;
        /**
         * amountInFiguers
         */
        @JSONField(name = "AmountInFiguers")
        private String amountInFiguers;
        /**
         * tollSign
         */
        @JSONField(name = "TollSign")
        private String tollSign;
        /**
         * zeroTaxRateIndicator
         */
        @JSONField(name = "ZeroTaxRateIndicator")
        private String zeroTaxRateIndicator;
        /**
         * carrier
         */
        @JSONField(name = "Carrier")
        private String carrier;
        /**
         * carrierCode
         */
        @JSONField(name = "CarrierCode")
        private String carrierCode;
        /**
         * recipient
         */
        @JSONField(name = "Recipient")
        private String recipient;
        /**
         * recipientCode
         */
        @JSONField(name = "RecipientCode")
        private String recipientCode;
        /**
         * receiver
         */
        @JSONField(name = "Receiver")
        private String receiver;
        /**
         * receiverCode
         */
        @JSONField(name = "ReceiverCode")
        private String receiverCode;
        /**
         * sender
         */
        @JSONField(name = "Sender")
        private String sender;
        /**
         * senderCode
         */
        @JSONField(name = "SenderCode")
        private String senderCode;
        /**
         * transportCargoInformation
         */
        @JSONField(name = "TransportCargoInformation")
        private String transportCargoInformation;
        /**
         * departureViaArrival
         */
        @JSONField(name = "DepartureViaArrival")
        private String departureViaArrival;
        /**
         * taxControlNum
         */
        @JSONField(name = "TaxControlNum")
        private String taxControlNum;
        /**
         * vehicleTypeNum
         */
        @JSONField(name = "VehicleTypeNum")
        private String vehicleTypeNum;
        /**
         * vehicleTonnage
         */
        @JSONField(name = "VehicleTonnage")
        private String vehicleTonnage;
        /**
         * noteDrawer
         */
        @JSONField(name = "NoteDrawer")
        private String noteDrawer;
        /**
         * checker
         */
        @JSONField(name = "Checker")
        private String checker;
        /**
         * payee
         */
        @JSONField(name = "Payee")
        private String payee;
        /**
         * remarks
         */
        @JSONField(name = "Remarks")
        private String remarks;
        /**
         * esvaturl
         */
        @JSONField(name = "ESVATURL")
        private String esvaturl;
        /**
         * listLabel
         */
        @JSONField(name = "ListLabel")
        private String listLabel;
        /**
         * commodityEndDate
         */
        @JSONField(name = "CommodityEndDate")
        private List<CommodityEndDate> commodityEndDate;
        /**
         * commodityVehicleType
         */
        @JSONField(name = "CommodityVehicleType")
        private List<CommodityVehicleType> commodityVehicleType;
        /**
         * commodityStartDate
         */
        @JSONField(name = "CommodityStartDate")
        private List<CommodityStartDate> commodityStartDate;
        /**
         * commodityPrice
         */
        @JSONField(name = "CommodityPrice")
        private List<CommodityPrice> commodityPrice;
        /**
         * commodityAmount
         */
        @JSONField(name = "CommodityAmount")
        private List<CommodityAmount> commodityAmount;
        /**
         * commodityType
         */
        @JSONField(name = "CommodityType")
        private List<CommodityType> commodityType;
        /**
         * commodityNum
         */
        @JSONField(name = "CommodityNum")
        private List<CommodityNum> commodityNum;
        /**
         * commodityTaxRate
         */
        @JSONField(name = "CommodityTaxRate")
        private List<CommodityTaxRate> commodityTaxRate;
        /**
         * commodityTax
         */
        @JSONField(name = "CommodityTax")
        private List<CommodityTax> commodityTax;
        /**
         * commodityPlateNum
         */
        @JSONField(name = "CommodityPlateNum")
        private List<CommodityPlateNum> commodityPlateNum;
        /**
         * commodityExpenseItem
         */
        @JSONField(name = "CommodityExpenseItem")
        private List<CommodityExpenseItem> commodityExpenseItem;
        /**
         * commodityUnit
         */
        @JSONField(name = "CommodityUnit")
        private List<CommodityUnit> commodityUnit;
        /**
         * commodityName
         */
        @JSONField(name = "CommodityName")
        private List<CommodityName> commodityName;

        /**
         * CommodityEndDate
         */
        @NoArgsConstructor
        @Data
        public static class CommodityEndDate {
            /**
             * row
             */
            @JSONField(name = "row")
            private String row;
            /**
             * word
             */
            @JSONField(name = "word")
            private String word;
        }

        /**
         * CommodityVehicleType
         */
        @NoArgsConstructor
        @Data
        public static class CommodityVehicleType {
            /**
             * row
             */
            @JSONField(name = "row")
            private String row;
        }

        /**
         * CommodityStartDate
         */
        @NoArgsConstructor
        @Data
        public static class CommodityStartDate {
            /**
             * row
             */
            @JSONField(name = "row")
            private String row;
            /**
             * word
             */
            @JSONField(name = "word")
            private String word;
        }

        /**
         * CommodityPrice
         */
        @NoArgsConstructor
        @Data
        public static class CommodityPrice {
            /**
             * row
             */
            @JSONField(name = "row")
            private String row;
            /**
             * word
             */
            @JSONField(name = "word")
            private String word;
        }

        /**
         * CommodityAmount
         */
        @NoArgsConstructor
        @Data
        public static class CommodityAmount {
            /**
             * row
             */
            @JSONField(name = "row")
            private String row;
            /**
             * word
             */
            @JSONField(name = "word")
            private String word;
        }

        /**
         * CommodityType
         */
        @NoArgsConstructor
        @Data
        public static class CommodityType {
            /**
             * row
             */
            @JSONField(name = "row")
            private String row;
            /**
             * word
             */
            @JSONField(name = "word")
            private String word;
        }

        /**
         * CommodityNum
         */
        @NoArgsConstructor
        @Data
        public static class CommodityNum {
            /**
             * row
             */
            @JSONField(name = "row")
            private String row;
            /**
             * word
             */
            @JSONField(name = "word")
            private String word;
        }

        /**
         * CommodityTaxRate
         */
        @NoArgsConstructor
        @Data
        public static class CommodityTaxRate {
            /**
             * row
             */
            @JSONField(name = "row")
            private String row;
            /**
             * word
             */
            @JSONField(name = "word")
            private String word;
        }

        /**
         * CommodityTax
         */
        @NoArgsConstructor
        @Data
        public static class CommodityTax {
            /**
             * row
             */
            @JSONField(name = "row")
            private String row;
            /**
             * word
             */
            @JSONField(name = "word")
            private String word;
        }

        /**
         * CommodityPlateNum
         */
        @NoArgsConstructor
        @Data
        public static class CommodityPlateNum {
            /**
             * row
             */
            @JSONField(name = "row")
            private String row;
            /**
             * word
             */
            @JSONField(name = "word")
            private String word;
        }

        /**
         * CommodityExpenseItem
         */
        @NoArgsConstructor
        @Data
        public static class CommodityExpenseItem {
            /**
             * row
             */
            @JSONField(name = "row")
            private String row;
            /**
             * word
             */
            @JSONField(name = "word")
            private String word;
        }

        /**
         * CommodityUnit
         */
        @NoArgsConstructor
        @Data
        public static class CommodityUnit {
            /**
             * row
             */
            @JSONField(name = "row")
            private String row;
            /**
             * word
             */
            @JSONField(name = "word")
            private String word;
        }

        /**
         * CommodityName
         */
        @NoArgsConstructor
        @Data
        public static class CommodityName {
            /**
             * row
             */
            @JSONField(name = "row")
            private String row;
            /**
             * word
             */
            @JSONField(name = "word")
            private String word;
        }
    }
}
