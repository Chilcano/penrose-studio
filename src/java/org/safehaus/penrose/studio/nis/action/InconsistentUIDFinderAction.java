package org.safehaus.penrose.studio.nis.action;

import org.safehaus.penrose.source.Source;
import org.safehaus.penrose.jdbc.adapter.JDBCAdapter;
import org.safehaus.penrose.jdbc.JDBCClient;
import org.safehaus.penrose.jdbc.QueryResponse;
import org.safehaus.penrose.jdbc.Assignment;
import org.safehaus.penrose.ldap.Attributes;
import org.safehaus.penrose.partition.Partition;
import org.safehaus.penrose.nis.NISDomain;

import java.util.Collection;
import java.util.ArrayList;
import java.sql.ResultSet;

/**
 * @author Endi S. Dewata
 */
public class InconsistentUIDFinderAction extends NISAction {

    public final static String CACHE_USERS = "cache_users";

    public InconsistentUIDFinderAction() throws Exception {
        setName("Inconsistent UID Finder");
        setDescription("Finds users with inconsistent UID numbers across domains");
    }

    public void execute(
            final NISActionRequest request,
            final NISActionResponse response
    ) throws Exception {

        String domainName1 = request.getDomain();
        NISDomain domain1 = nisTool.getNisDomains().get(domainName1);

        for (String domainName2 : nisTool.getNisDomains().keySet()) {
            if (domainName1.equals(domainName2)) continue;

            NISDomain domain2 = nisTool.getNisDomains().get(domainName2);
            execute(domain1, domain2, response);
        }

        response.close();
    }

    public void execute(
            final NISDomain domain1,
            final NISDomain domain2,
            final NISActionResponse response
    ) throws Exception {

        log.debug("Checking conflicts between "+domain1.getName()+" and "+domain2.getName()+".");

        final Partition partition1 = nisTool.getPartitions().getPartition(domain1.getPartition());
        final Source source1 = partition1.getSource(CACHE_USERS);

        final Partition partition2 = nisTool.getPartitions().getPartition(domain2.getPartition());
        final Source source2 = partition2.getSource(CACHE_USERS);

        JDBCAdapter adapter1 = (JDBCAdapter)source1.getConnection().getAdapter();
        JDBCClient client1 = adapter1.getClient();

        String table1 = client1.getTableName(source1);
        String table2 = client1.getTableName(source2);

        String sql = "select a.uid, a.uidNumber, b.uidNumber, c.uid, c.uidNumber, d.uidNumber" +
                " from "+table1+" a"+
                " left join nis.users b on b.domain=? and a.uid=b.uid"+
                " join "+table2+" c on a.uid = c.uid "+
                " left join nis.users d on d.domain=? and c.uid=d.uid"+
                " where b.uidNumber is null and d.uidNumber is null and a.uidNumber <> c.uidNumber"+
                    " or b.uidNumber is null and a.uidNumber <> d.uidNumber"+
                    " or d.uidNumber is null and b.uidNumber <> c.uidNumber"+
                    " or b.uidNumber <> d.uidNumber" +
                " order by a.uid";

        Collection<Assignment> assignments = new ArrayList<Assignment>();
        assignments.add(new Assignment(domain1.getName()));
        assignments.add(new Assignment(domain2.getName()));

        QueryResponse queryResponse = new QueryResponse() {
            public void add(Object object) throws Exception {
                ResultSet rs = (ResultSet)object;

                String uid1 = rs.getString(1);
                Object origUidNumber1 = rs.getObject(2);
                Object uidNumber1 = rs.getObject(3);

                String uid2 = rs.getString(4);
                Object origUidNumber2 = rs.getObject(5);
                Object uidNumber2 = rs.getObject(6);

                Attributes attributes1 = new Attributes();
                attributes1.setValue("domain", domain1.getName());
                attributes1.setValue("partition", domain1.getPartition());
                attributes1.setValue("uid", uid1);
                attributes1.setValue("origUidNumber", origUidNumber1);
                attributes1.setValue("uidNumber", uidNumber1);

                Attributes attributes2 = new Attributes();
                attributes2.setValue("domain", domain2.getName());
                attributes2.setValue("partition", domain2.getPartition());
                attributes2.setValue("uid", uid2);
                attributes2.setValue("origUidNumber", origUidNumber2);
                attributes2.setValue("uidNumber", uidNumber2);

                response.add(new Conflict(attributes1, attributes2));
            }
        };

        client1.executeQuery(sql, assignments, queryResponse);
    }
}