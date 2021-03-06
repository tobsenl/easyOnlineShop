package cn.sqkj.nsyl.userManager.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import cn.sqkj.nsyl.userManager.pojo.NsUserPurse;
import framework.bean.PageBean;
import framework.db.DBUtil;
import framework.db.PageDAO;
@Repository("userPurseDAO")
public class UserPurseDAO extends PageDAO{
	
	/**
	 * 获取用户总记录数
	 * @param sql
	 * @param params
	 * @return
	 */
	public int findNsUserPurseCount(String sql, List params){
		return DBUtil.getDBUtilByRequest().queryCountBySQL(sql,params);
	}
	
	/**
	 * 获取用户数据集
	 * @param sql
	 * @param params
	 * @param pageBean
	 * @return
	 */
	public List<NsUserPurse> findNsUserPursePage(String sql,List params, PageBean pageBean) {
		List<NsUserPurse> pageData = this.query(sql, params, pageBean);
		return pageData;
	}
}
