package org.joget.marketplace;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.dao.FormDataDao;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.LongTermCache;
import org.joget.commons.util.SecurityUtil;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.model.service.WorkflowUserManager;
import org.joget.workflow.util.WorkflowUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class EmoteFormElement extends Element implements FormBuilderPaletteElement, PluginWebSupport {

    private final static String MESSAGE_PATH = "messages/EmoteFormElement";

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String template = "emoteFormElement.ftl";
        String value = FormUtil.getElementPropertyValue(this, formData);
        String id = formData.getPrimaryKeyValue();

        String emoteField = getPropertyString("emoteField");
        String foreignKeyField = getPropertyString("foreignKeyField");
        String formDefId = getPropertyString("formDefId");

        // css styling
        dataModel.put("btnIcon", getPropertyString("btnIcon"));
        dataModel.put("btnBgColor", getPropertyString("btnBgColor"));
        dataModel.put("btnIconColor", getPropertyString("btnIconColor"));
        dataModel.put("reactbtnBgColor", getPropertyString("reactbtnBgColor"));
        dataModel.put("reactbtnTextColor", getPropertyString("reactbtnTextColor"));
        dataModel.put("reactbtnReactedBgColor", getPropertyString("reactbtnReactedBgColor"));
        dataModel.put("reactbtnReactedTextColor", getPropertyString("reactbtnReactedTextColor"));

        FormRowSet emoteList = null;

        String config = getConfigString();

        WorkflowUserManager wum = (WorkflowUserManager) AppUtil.getApplicationContext().getBean("workflowUserManager");
        boolean anonymous = wum.isCurrentUserAnonymous();
        dataModel.put("anonymous", anonymous ? "true" : "false");
        if (FormUtil.isFormBuilderActive()) {
            dataModel.put("emoteList", emoteList);
            id = "";
        } else {
            LongTermCache longTermCache = (LongTermCache) AppUtil.getApplicationContext().getBean("longTermCache");
            HttpServletRequest httpServletRequest = WorkflowUtil.getHttpServletRequest();
            emoteList = getEmote(formDefId, foreignKeyField, id, emoteField, longTermCache);
            dataModel.put("emoteList", emoteList);
        }

        dataModel.put("id", id);
        String html = FormUtil.generateElementHtml(this, formData, template, dataModel);
        return html;
    }

    public String getServiceUrl() {
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        String url = WorkflowUtil.getHttpServletRequest().getContextPath() + "/web/json/app/" + appDef.getAppId() + "/" + appDef.getVersion() + "/plugin/org.joget.marketplace.EmoteFormElement/service";
        //create nonce
        String paramName = "form-contents";
        String formDefId = getPropertyString("formDefId");
        String nonce = SecurityUtil.generateNonce(new String[]{"ContentLikeDislike", appDef.getAppId(), appDef.getVersion().toString(), paramName}, 1);
        setProperty("nonce", nonce);
        try {
            url = url + "?_nonce=" + URLEncoder.encode(nonce, "UTF-8") + "&_paramName=" + URLEncoder.encode(paramName, "UTF-8") + "&_formDefId=" + formDefId;
        } catch (Exception e) {
        }
        return url;
    }

    public String getConfigString() {
        String config = "";
        try {
            JSONObject pluginProperties = FormUtil.generatePropertyJsonObject(getProperties());
            config = pluginProperties.toString();
        } catch (JSONException ex) {
            LogUtil.error(getClassName(), ex, ex.getMessage());
        }
        config = SecurityUtil.encrypt(config);
        return config;
    }

    @Override
    public String getName() {
        return AppPluginUtil.getMessage("org.joget.marketplace.emote.element.pluginLabel", getClassName(), MESSAGE_PATH);
    }

    @Override
    public String getVersion() {
        return Activator.VERSION;
    }

    @Override
    public String getDescription() {
        return AppPluginUtil.getMessage("org.joget.marketplace.emote.element.pluginDesc", getClassName(), MESSAGE_PATH);
    }

    @Override
    public String getLabel() {
        return AppPluginUtil.getMessage("org.joget.marketplace.emote.element.pluginLabel", getClassName(), MESSAGE_PATH);
    }

    @Override
    public String getClassName() {
        return this.getClass().getName();
    }

    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClass().getName(), "/properties/emoteFormElement.json", null, true, MESSAGE_PATH);
    }

    @Override
    public String getFormBuilderCategory() {
        return "Marketplace";
    }

    @Override
    public int getFormBuilderPosition() {
        return 500;
    }

    @Override
    public String getFormBuilderIcon() {
        return "<i class=\"fas fa-smile\"></i>";
    }

    @Override
    public String getFormBuilderTemplate() {
        return "<label class='label'>"+getLabel()+"</label>";
    }

    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            LongTermCache longTermCache = (LongTermCache) AppUtil.getApplicationContext().getBean("longTermCache");
            AppDefinition appDef = AppUtil.getCurrentAppDefinition();
            String nonce = request.getParameter("_nonce");
            String paramName = request.getParameter("_paramName");
            String fkValue = request.getParameter("id");
            String action = request.getParameter("action");
            String formDefId = "";
            String fkField = "";
            String emoteField = "";
            if (SecurityUtil.verifyNonce(nonce, new String[]{"ContentLikeDislike", appDef.getAppId(), appDef.getVersion().toString(), paramName})) {
                String config = SecurityUtil.decrypt(request.getParameter("config"));
                JSONObject c = new JSONObject(config);
                if (nonce.equals(c.getString("nonce"))) {
                    formDefId = c.getString("formDefId");
                    emoteField = c.getString("emoteField");
                    fkField = c.getString("foreignKeyField");
                    String condition = "where e.customProperties." + fkField + " = ? AND e.customProperties." + emoteField + " like ? AND e.createdBy = ?" ;
                    String clickedValue = request.getParameter("clickedValue");
                    AppService appService = (AppService) FormUtil.getApplicationContext().getBean("appService");
                    FormDataDao formDataDao = (FormDataDao) AppUtil.getApplicationContext().getBean("formDataDao");
                    String tableName = appService.getFormTableName(appDef, formDefId);
                    String username = WorkflowUtil.getCurrentUsername();
                    FormRowSet choosenRow = new FormRowSet();
                    FormRowSet allFKRows = new FormRowSet();

                    // get from cache, if null then find from db
                    net.sf.ehcache.Element el = longTermCache.get(fkValue);
                    if (el != null) {
                        allFKRows = (FormRowSet) el.getObjectValue();
                        for (FormRow fr : allFKRows) {
                            if (fr.get(fkField).equals(fkValue) && fr.get(emoteField).equals(clickedValue) && fr.get("createdBy").equals(username)) {
                                choosenRow.add(fr);
                            }
                        }
                    } else {
                        choosenRow = formDataDao.find(formDefId, tableName, condition, new String[]{fkValue, clickedValue, username}, null, null, null, null);
                    }

                    if (action.equals("add")) {
                        // if row is found, dont allow save duplicate emote
                        if (choosenRow != null && !choosenRow.isEmpty()) {
                          
                        } else {
                            storeEmote(clickedValue, fkValue, formDefId, tableName, fkField, emoteField, appService, longTermCache);
                        }
                    } else if (action.equals("remove")) {
                        if (choosenRow != null && !choosenRow.isEmpty()) {
                            formDataDao.delete(formDefId, tableName, choosenRow);

                            // remove from cache
                            if (el != null) {
                                allFKRows.removeAll(choosenRow);
                                net.sf.ehcache.Element elUpdate = new net.sf.ehcache.Element(fkValue, allFKRows);
                                longTermCache.remove(fkValue);
                                longTermCache.put(elUpdate);
                            }
                        }
                    }
                }
            }
            FormRowSet emoteList = getEmote(formDefId, fkField, fkValue, emoteField, longTermCache);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("emoteList", emoteList);
            response.setContentType("application/json");
            response.getWriter().write(jsonObject.toString());
        }
    }

    private FormRowSet getEmote(String formDefId, String foreignKeyField, String foreignKeyValue, String fieldId, LongTermCache longTermCache) {
        AppService appService = (AppService) FormUtil.getApplicationContext().getBean("appService");
        FormDataDao formDataDao = (FormDataDao) AppUtil.getApplicationContext().getBean("formDataDao");
        AppDefinition appDef = AppUtil.getCurrentAppDefinition();
        String tableName = appService.getFormTableName(appDef, formDefId);
        String condition = "where e.customProperties." + foreignKeyField + " = ?";
        String emoteArr = "";
        FormRowSet rows = formDataDao.find(formDefId, tableName, condition, new String[]{foreignKeyValue}, FormUtil.PROPERTY_DATE_MODIFIED, true, 0, null);

        // get from cache, compare with db whether accurate
        net.sf.ehcache.Element el = longTermCache.get(foreignKeyValue);
        if (el != null) {
            FormRowSet rowsCache = (FormRowSet) el.getObjectValue();

            if(!rowsCache.equals(rows)){
                net.sf.ehcache.Element elUpdate = new net.sf.ehcache.Element(foreignKeyValue, rows);
                longTermCache.remove(foreignKeyValue);
                longTermCache.put(elUpdate);
            }
        }

        rows = countAndAggregateValues(rows, fieldId);
        return rows;
    }

    private FormRowSet countAndAggregateValues(FormRowSet rows, String fieldId) {
        // store counts for each key
        Map<String, Integer> countMap = new HashMap<>();
        // store associated values for each key
        Map<String, List<String>> valueMap = new HashMap<>();

        for (FormRow row : rows) {
            String key = row.getProperty(fieldId);
            String value = row.getProperty("createdByName");

            countMap.put(key, countMap.getOrDefault(key, 0) + 1);

           
            if (!valueMap.containsKey(key)) {
                valueMap.put(key, new ArrayList<String>());
            }
            valueMap.get(key).add(value);
        }

        // new list to store the counted and aggregated results
        FormRowSet countedList = new FormRowSet();

        // Merge countMap and valueMap to produce the final output
        for (String key : countMap.keySet()) {
            FormRow countedEntry = new FormRow();
            countedEntry.put("emote", key);
            countedEntry.put("count", countMap.get(key));
            countedEntry.put("createdByName", joinList(valueMap.get(key)));

            if(joinList(valueMap.get(key)).contains(WorkflowUtil.getCurrentUserFullName())){
                countedEntry.put("sameUser", true);
            } else {
                countedEntry.put("sameUser", false);

            }

            countedList.add(countedEntry);
        }

        return countedList;
    }

    // join list of strings with a comma
    private String joinList(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    private void storeEmote(String clickedValue, String fkId, String formDefId, String tableName, String fkField, String fieldId, AppService appService, LongTermCache longTermCache) {
        FormRowSet rows = new FormRowSet();
        FormRow row = new FormRow();
        row.put(fkField, fkId);
        row.put(fieldId, clickedValue);
        rows.add(row);
        rows = appService.storeFormData(formDefId, tableName, rows, null);

        FormRowSet rowsCache = new FormRowSet();
        // get from existing cache
        net.sf.ehcache.Element el = longTermCache.get(fkId);
        if (el != null) {
            rowsCache = (FormRowSet) el.getObjectValue();
        }

        // store into db and cache
        rows.addAll(rowsCache);
        net.sf.ehcache.Element elStore = new net.sf.ehcache.Element(fkId, rows);
        longTermCache.put(elStore);
    }    
}