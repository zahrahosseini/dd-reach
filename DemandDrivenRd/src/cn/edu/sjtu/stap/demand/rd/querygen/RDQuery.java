package cn.edu.sjtu.stap.demand.rd.querygen;

import cn.edu.sjtu.stap.demand.rd.RdProgramPoint;

public class RDQuery {
	private RdProgramPoint dataItem;
	private RdProgramPoint stmt;

	public RdProgramPoint getDataItem() {
		return dataItem;
	}
	public void setDataItem(RdProgramPoint dataItem) {
		this.dataItem = dataItem;
	}
	public RdProgramPoint getStmt() {
		return stmt;
	}
	public void setStmt(RdProgramPoint stmt) {
		this.stmt = stmt;
	}
}
