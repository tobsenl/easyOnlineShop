package framework.action.system;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import net.sf.json.JSONArray;
import framework.bean.UserMsg;
import framework.db.DBUtil;
import framework.db.pojo.TXtMenu;
import framework.helper.RequestHelper;
import framework.util.ComparatorMenuVO;
import framework.util.DateUtils;

import com.opensymphony.xwork2.Action;

public class MenuTreeAction {
	private List<Map> trees;
	private TXtMenu tree;
	private String filePath;
	
	public String fileTree(){
		String realPath = RequestHelper.getServletContext().getRealPath("/");
		
		if(filePath==null){
			filePath = "";
		}
		if(filePath.indexOf("WEB-INF")>-1){
			return null;
		}
		
		File file = new File(realPath+filePath);
		List list = new ArrayList();
		for(File f : file.listFiles()){
			if(f.getName().indexOf("WEB-INF")>-1){
				continue;
			}
			Map map = new HashMap();
			map.put("id", f.getPath().replace(realPath, "").replaceAll("\\\\", "/"));
			map.put("text", f.getName());
			if(f.isDirectory()){
				map.put("state", "closed");
			}
			
			list.add(map);
		}
		try {
			RequestHelper.wirte(JSONArray.fromObject(list).toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String delete(){
		DBUtil db = DBUtil.getDBUtilByRequest();
		if(this.tree.getMenuId().indexOf("xt")==0){
			return Action.ERROR;
		}
		//String sql = "select * from t_xt_menu where menu_parent_ids like (select menu_parent_ids from t_xt_menu where menu_id=?)||'%'";
		
		//查询是否有子菜单
		String existsChildMenuSql = "select m.menu_id from t_xt_menu m where m.menu_parent_id=?";
		List<TXtMenu> childMenus = db.queryBySQL(existsChildMenuSql, this.tree.getMenuId());
		if(childMenus != null && !childMenus.isEmpty()) {
			return Action.ERROR;
		}
		
		String deleteMenuSql = "delete from t_xt_menu where menu_id=?";
		db.executeSQL(deleteMenuSql, this.tree.getMenuId());
		db.commit();
		return Action.SUCCESS;
	}
	
	public String save(){
		DBUtil db = DBUtil.getDBUtilByRequest();
		if(this.tree.getMenuId()==null || "".equals(this.tree.getMenuId().trim())){
			this.tree.setMenuId(UUID.randomUUID().toString());
			this.tree.setLrSj(DateUtils.getTimestamp());
			this.tree.setMenuParentIds(tree.getMenuParentIds().toString()+","+tree.getMenuId());
			this.tree.setYxBj("1");
			db.insert(this.tree);
		}else{
			Map params = new HashMap(1);
			params.put("menuId", tree.getMenuId());
			TXtMenu menu = (TXtMenu)db.queryByPojo(TXtMenu.class, params).get(0);
			menu.setPxXh(tree.getPxXh());
			menu.setUrl(tree.getUrl());
			menu.setMenuName(tree.getMenuName());
			menu.setOpenMethod(tree.getOpenMethod());
			menu.setLogo(tree.getLogo());
			this.tree.setLrSj(DateUtils.getTimestamp());
			db.update(menu);
		}
		return Action.SUCCESS;
	}
	
	public String menuTreeAll(){
		menuTreeQuery(true);
		return Action.SUCCESS;
	}
	
	
	public String menuTree(){
		menuTreeQuery(false);
		return Action.SUCCESS;
	}
	
	private static final String SQL_MENU = "select m.menu_id,m.menu_name,m.menu_parent_id,m.url,m.px_xh, m.menu_parent_ids, m.open_method, m.logo " 
		+" from t_xt_menu m,t_xt_role_menu rm"
		+" where m.menu_id=rm.menu_id and m.yx_bj='1' and rm.yx_bj='1'"
		+" and rm.role_id in (?)  order by m.px_xh";
	private static final String SQL_MENU_ALL = "select m.menu_id,m.menu_name,m.menu_parent_id,m.url,m.px_xh, m.menu_parent_ids, m.open_method, m.logo " 
		+" from t_xt_menu m"
		+" where m.yx_bj='1' order by px_xh";
	@SuppressWarnings("unchecked")
	private void menuTreeQuery(boolean isAll){
		DBUtil db = DBUtil.getDBUtilByRequest();
		List<Map> list = null;
		if(isAll){
			list = db.queryBySQL(SQL_MENU_ALL);
		} else {
			UserMsg currUser = (UserMsg)RequestHelper.getSession().getAttribute("user");
			String rolestr = currUser.toRoleString();
			String sql = SQL_MENU.replaceFirst("\\?", rolestr);
			list = db.queryBySQL(sql);
		}
		Map root = null;
		Map<String,Map> temp = new TreeMap();
		for (Map pojo : list) {
			Map node = new TreeMap();
			node.put("id", pojo.get("menuId"));
			node.put("text", pojo.get("menuName"));
			node.put("parentId", pojo.get("menuParentId"));
			Map temp1 = new HashMap(4);
			temp1.put("pxXh", pojo.get("pxXh"));
			temp1.put("menuParentIds", pojo.get("menuParentIds"));
			temp1.put("openMethod", pojo.get("openMethod"));
			temp1.put("logo", pojo.get("logo"));
			if(pojo.get("url")!=null && pojo.get("url").toString().length()!=0){
				temp1.put("url", pojo.get("url"));
			}else{
				temp1.put("url", "");
				node.put("state", "closed");
			}
			node.put("attributes", temp1);
			temp.put(pojo.get("menuId").toString(), node);
			if("root".equals(node.get("parentId"))){
				root = node;
			}
		}
		
		for(String key : temp.keySet()){
			Map node = temp.get(key);
			Map parentMap = temp.get(node.get("parentId"));
			if(parentMap!=null){
				if(parentMap.get("children")==null){
					parentMap.put("children", new ArrayList());
				}
				((ArrayList)parentMap.get("children")).add(node);
			}
		}
		
		//循环判断是否有子节点，是否能展开
		for(String key : temp.keySet()) {
			Map node = temp.get(key);
			if(node.get("children") != null) {
				List childOrgList = (ArrayList) node.get("children");
				if(!childOrgList.isEmpty()) {
					Collections.sort(childOrgList, new ComparatorMenuVO());
				}
			} 
		}
		
		trees = new ArrayList(1);
		if(root!=null){
			trees.add(root);
		}
	}
	
	public List getTrees() {
		return trees;
	}

	public TXtMenu getTree() {
		return tree;
	}

	public void setTree(TXtMenu tree) {
		this.tree = tree;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	
}
