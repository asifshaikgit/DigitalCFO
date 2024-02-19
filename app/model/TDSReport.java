package model;

import java.util.Date;

public class TDSReport {
		
		public String section;
		public String dateOfTxn;
		public String itemName;
		public String vendorName;
		public String rate;
		public String grossAmt;
		public String tdsAmt;
		
		public String getSection() {
			return section;
		}
		public void setSection(String section) {
			this.section = section;
		}
		public String getDateOfTxn() {
			return dateOfTxn;
		}
		public void setDateOfTxn(String dateOfTxn) {
			this.dateOfTxn = dateOfTxn;
		}
		public String getItemName() {
			return itemName;
		}
		public void setItemName(String itemName) {
			this.itemName = itemName;
		}
		public String getVendorName() {
			return vendorName;
		}
		public void setVendorName(String vendorName) {
			this.vendorName = vendorName;
		}
		public String getRate() {
			return rate;
		}
		public void setRate(String rate) {
			this.rate = rate;
		}
		public String getGrossAmt() {
			return grossAmt;
		}
		public void setGrossAmt(String grossAmt) {
			this.grossAmt = grossAmt;
		}
		public String getTdsAmt() {
			return tdsAmt;
		}
		public void setTdsAmt(String tdsAmt) {
			this.tdsAmt = tdsAmt;
		}
}
