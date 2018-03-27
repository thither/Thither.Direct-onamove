package direct.thither.onamove.properties;

import org.json.JSONObject;

import java.util.Iterator;

public class QueryParams{
    private JSONObject m_params;
    public QueryParams (){ m_params = new JSONObject();}
    public QueryParams (String v){
        try {
            m_params = new JSONObject(v);
        }catch (Exception e){
            m_params = new JSONObject();
        }
    }
    public synchronized void unset_param(String k){ m_params.remove(k);}
    public synchronized void set_param(String k, String v){
        try {m_params.put(k, v); } catch (Exception e) { }
    }
    public void set_param(String k, int v){     set_param(k, String.valueOf(v)); }
    public void set_param(String k, boolean v){ set_param(k, String.valueOf(v)); }
    public void set_param(String k, long v){    set_param(k, String.valueOf(v)); }
    public void set_param(String k, double v){  set_param(k, String.valueOf(v)); }
    public synchronized String get_param(String k){
        try {return m_params.getString(k); } catch (Exception e) { }
        return null;
    }

    public synchronized String get_query_params(){
        String k;
        StringBuilder output = new StringBuilder();
        Object p;
        Iterator itr = m_params.keys();
        while(itr.hasNext()) {
            try {
                p = itr.next();
                k = p.toString();
                output.append(k).append("=").append(m_params.getString(k)).append("&");
            } catch (Exception e) { }
        }
        return output.toString();
    }
    public synchronized String toString(){return m_params.toString();}
}
