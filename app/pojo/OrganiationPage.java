package pojo;

import model.Organization;

import java.util.List;

public class OrganiationPage {
	private final int pageSize;
	private final long totalRowCount;
	private final int pageIndex;
	private List<Organization> list;

	public OrganiationPage(long total, int page, int pageSize) {
		this.totalRowCount = total;
		this.pageIndex = page;
		this.pageSize = pageSize;
	}

	public long getTotalRowCount() {
		return totalRowCount;
	}

	public int getPageIndex() {
		return pageIndex;
	}

	public List<Organization> getList() {
		return list;
	}

	public void setList(List<Organization> l) {
		list = l;
	}

	public boolean hasPrev() {
		return pageIndex > 1;
	}

	public boolean hasNext() {
		return (totalRowCount/pageSize) >= pageIndex;
	}

	public String getDisplayXtoYofZ() {
		int start = ((pageIndex - 1) * pageSize + 1);
		int end = start + Math.min(pageSize, list.size()) - 1;
		return start + " to " + end + " of " + totalRowCount;
	}

}