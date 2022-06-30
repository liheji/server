package top.liheji.server.pojo.other;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author : Galaxy
 * @time : 2022/5/9 20:51
 * @create : IdeaJ
 * @project : serverPlus
 * @description : 发票信息
 */
@NoArgsConstructor
@Data
public class Invoice {
    /**
     * wordsResult
     */
    @JSONField(name = "words_result")
    private WordsResult wordsResult;
    /**
     * wordsResultNum
     */
    @JSONField(name = "words_result_num")
    private Integer wordsResultNum;
    /**
     * pdfFileSize
     */
    @JSONField(name = "pdf_file_size")
    private Integer pdfFileSize;
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
         * amountInWords
         */
        @JSONField(name = "AmountInWords")
        private String amountInWords;
        /**
         * invoiceNumConfirm
         */
        @JSONField(name = "InvoiceNumConfirm")
        private String invoiceNumConfirm;
        /**
         * commodityEndDate
         */
        @JSONField(name = "CommodityEndDate")
        private List<?> commodityEndDate;
        /**
         * commodityStartDate
         */
        @JSONField(name = "CommodityStartDate")
        private List<?> commodityStartDate;
        /**
         * commodityVehicleType
         */
        @JSONField(name = "CommodityVehicleType")
        private List<?> commodityVehicleType;
        /**
         * commodityPrice
         */
        @JSONField(name = "CommodityPrice")
        private List<CommodityPrice> commodityPrice;
        /**
         * invoiceTag
         */
        @JSONField(name = "InvoiceTag")
        private String invoiceTag;
        /**
         * noteDrawer
         */
        @JSONField(name = "NoteDrawer")
        private String noteDrawer;
        /**
         * sellerAddress
         */
        @JSONField(name = "SellerAddress")
        private String sellerAddress;
        /**
         * commodityNum
         */
        @JSONField(name = "CommodityNum")
        private List<CommodityNum> commodityNum;
        /**
         * sellerRegisterNum
         */
        @JSONField(name = "SellerRegisterNum")
        private String sellerRegisterNum;
        /**
         * machineCode
         */
        @JSONField(name = "MachineCode")
        private String machineCode;
        /**
         * remarks
         */
        @JSONField(name = "Remarks")
        private String remarks;
        /**
         * sellerBank
         */
        @JSONField(name = "SellerBank")
        private String sellerBank;
        /**
         * commodityTaxRate
         */
        @JSONField(name = "CommodityTaxRate")
        private List<CommodityTaxRate> commodityTaxRate;
        /**
         * serviceType
         */
        @JSONField(name = "ServiceType")
        private String serviceType;
        /**
         * totalTax
         */
        @JSONField(name = "TotalTax")
        private String totalTax;
        /**
         * invoiceCodeConfirm
         */
        @JSONField(name = "InvoiceCodeConfirm")
        private String invoiceCodeConfirm;
        /**
         * checkCode
         */
        @JSONField(name = "CheckCode")
        private String checkCode;
        /**
         * invoiceCode
         */
        @JSONField(name = "InvoiceCode")
        private String invoiceCode;
        /**
         * invoiceDate
         */
        @JSONField(name = "InvoiceDate")
        private String invoiceDate;
        /**
         * purchaserRegisterNum
         */
        @JSONField(name = "PurchaserRegisterNum")
        private String purchaserRegisterNum;
        /**
         * invoiceTypeOrg
         */
        @JSONField(name = "InvoiceTypeOrg")
        private String invoiceTypeOrg;
        /**
         * password
         */
        @JSONField(name = "Password")
        private String password;
        /**
         * onlinePay
         */
        @JSONField(name = "OnlinePay")
        private String onlinePay;
        /**
         * agent
         */
        @JSONField(name = "Agent")
        private String agent;
        /**
         * amountInFiguers
         */
        @JSONField(name = "AmountInFiguers")
        private String amountInFiguers;
        /**
         * purchaserBank
         */
        @JSONField(name = "PurchaserBank")
        private String purchaserBank;
        /**
         * checker
         */
        @JSONField(name = "Checker")
        private String checker;
        /**
         * city
         */
        @JSONField(name = "City")
        private String city;
        /**
         * totalAmount
         */
        @JSONField(name = "TotalAmount")
        private String totalAmount;
        /**
         * commodityAmount
         */
        @JSONField(name = "CommodityAmount")
        private List<CommodityAmount> commodityAmount;
        /**
         * purchaserName
         */
        @JSONField(name = "PurchaserName")
        private String purchaserName;
        /**
         * commodityType
         */
        @JSONField(name = "CommodityType")
        private List<?> commodityType;
        /**
         * province
         */
        @JSONField(name = "Province")
        private String province;
        /**
         * invoiceType
         */
        @JSONField(name = "InvoiceType")
        private String invoiceType;
        /**
         * sheetNum
         */
        @JSONField(name = "SheetNum")
        private String sheetNum;
        /**
         * purchaserAddress
         */
        @JSONField(name = "PurchaserAddress")
        private String purchaserAddress;
        /**
         * commodityTax
         */
        @JSONField(name = "CommodityTax")
        private List<CommodityTax> commodityTax;
        /**
         * commodityPlateNum
         */
        @JSONField(name = "CommodityPlateNum")
        private List<?> commodityPlateNum;
        /**
         * commodityUnit
         */
        @JSONField(name = "CommodityUnit")
        private List<CommodityUnit> commodityUnit;
        /**
         * payee
         */
        @JSONField(name = "Payee")
        private String payee;
        /**
         * commodityName
         */
        @JSONField(name = "CommodityName")
        private List<CommodityName> commodityName;
        /**
         * sellerName
         */
        @JSONField(name = "SellerName")
        private String sellerName;
        /**
         * invoiceNum
         */
        @JSONField(name = "InvoiceNum")
        private String invoiceNum;

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
