package model.balancesheet;

/**
 * @author myidos
 */
public class BalanceSheetDAOBean {
	private String curName;
	private Double curFigure;
	private String prevName;
	private Double prevFigure;
	/**
	 * @return the curName
	 */
	public final String getCurName() {
		return curName;
	}
	/**
	 * @param curName the curName to set
	 */
	public final void setCurName(String curName) {
		this.curName = curName;
	}
	/**
	 * @return the curFigure
	 */
	public final Double getCurFigure() {
		return curFigure;
	}
	/**
	 * @param curFigure the curFigure to set
	 */
	public final void setCurFigure(Double curFigure) {
		this.curFigure = curFigure;
	}
	/**
	 * @return the prevName
	 */
	public final String getPrevName() {
		return prevName;
	}
	/**
	 * @param prevName the prevName to set
	 */
	public final void setPrevName(String prevName) {
		this.prevName = prevName;
	}
	/**
	 * @return the prevFigure
	 */
	public final Double getPrevFigure() {
		return prevFigure;
	}
	/**
	 * @param prevFigure the prevFigure to set
	 */
	public final void setPrevFigure(Double prevFigure) {
		this.prevFigure = prevFigure;
	}	
}
