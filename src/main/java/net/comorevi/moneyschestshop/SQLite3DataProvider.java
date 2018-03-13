package net.comorevi.moneyschestshop;

import cn.nukkit.Player;
import cn.nukkit.block.Block;

import java.sql.*;
import java.util.LinkedHashMap;

public class SQLite3DataProvider {

    MoneySChestShop mainClass;
    String path;

    public SQLite3DataProvider(MoneySChestShop plugin) {
        this.mainClass = plugin;
        this.path = plugin.getDataFolder().toString();
        connect();
    }

    public void removeShopBySign(Object[] condition) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + this.path + "/DataDB.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            
            statement.executeUpdate("delete from ChestShop where signX = " + condition[0] + " and signY = " + condition[1] + " and signZ = " + condition[2] + " and level = '" + condition[3] + "'");
        } catch(SQLException e) {
            System.err.println(e.getMessage() + " : at removeShopSign");
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                	System.out.println(e.getMessage() + " : at removeShopSign on close connection");
                	e.printStackTrace();
                }
            }
        }
    }
    
    public void removeShopByChest(Object[] condition) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + this.path + "/DataDB.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            
            statement.executeUpdate("delete from ChestShop where chestX = " + condition[0] + " and chestY = " + condition[1] + " and chestZ = " + condition[2] + " and level = '" + condition[3] + "'");
        } catch(SQLException e) {
            System.err.println(e.getMessage() + " : at removeShopChest");
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                	System.out.println(e.getMessage() + " : at removeShopChest on close connection");
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * register shop to database
     *
     * @param shopOwner
     * @param saleNum
     * @param price
     * @param productID
     * @param sign
     * @param chest
     * @return void
     */
    public void registerShop(String shopOwner, int saleNum, int price, int productID, int productMeta, Block sign, Block chest) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + this.path + "/DataDB.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            statement.executeUpdate("insert into ChestShop (shopOwner, saleNum, price, productID, productMeta, signX, signY, signZ, chestX, chestY, chestZ , level) values ('"+shopOwner+"', "+saleNum+", "+price+", "+productID+", "+productMeta+", "+sign.x+", "+sign.y+", "+sign.z+", "+chest.x+", "+chest.y+", "+chest.z+", '"+sign.getLevel().getName()+"')");
        } catch(SQLException e) {
            System.err.println(e.getMessage() + " : at registerShop");
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                	System.out.println(e.getMessage() + " : at registerShop on close connection");
                    e.printStackTrace();
                }
            }
        }
    }
    
    public void updateShop(String shopOwner, int saleNum, int price, int productID, int productMeta, Block sign) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + this.path + "/DataDB.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            
            Object[] signCondition = {(int) sign.x, (int) sign.y, (int) sign.z};
            LinkedHashMap<String, Object> shopSignInfo = getShopInfo(signCondition);
            
            statement.executeUpdate("update ChestShop set shopOwner = '" + shopOwner + "', saleNum = " + saleNum + ", price = " + price + ", productID = " + productID + ", productMeta = " + productMeta + " where id = " + shopSignInfo.get("id"));
        } catch(SQLException e) {
            System.err.println(e.getMessage() + " : at updateShop");
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.out.println(e.getMessage() + " : at updateShop on close connection");
                    e.printStackTrace();
                }
            }
        }
    }
    
    public LinkedHashMap<String, Object> getShopInfo(Object[] condition) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + this.path + "/DataDB.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            ResultSet rs = statement.executeQuery("select * from ChestShop where signX = " + condition[0] + " and signY = " + condition[1] + " and signZ = " + condition[2] + " and level = '" + condition[3] + "'");
            LinkedHashMap<String, Object> shopInfo = new LinkedHashMap<String, Object>(){{
                put("id", rs.getInt("id"));
                put("shopOwner", rs.getString("shopOwner"));
                put("saleNum", rs.getInt("saleNum"));
                put("price", rs.getInt("price"));
                put("productID", rs.getInt("productID"));
                put("productMeta", rs.getInt("productMeta"));
                put("signX", rs.getInt("signX"));
                put("signY", rs.getInt("signY"));
                put("signZ", rs.getInt("signZ"));
                put("chestX", rs.getInt("chestX"));
                put("chestY", rs.getInt("chestY"));
                put("chestZ", rs.getInt("chestZ"));
            }};
                
            return shopInfo;
        } catch(SQLException e) {
            System.err.println(e.getMessage() + " : at getShopInfo");
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                	System.out.println(e.getMessage() + " : at getShopinfo on close connection");
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    
    public boolean isShopOwner(Object[] condition, Player player) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + this.path + "/DataDB.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
    
            ResultSet rs = statement.executeQuery("select * from ChestShop where signX = " + condition[0] + " and signY = " + condition[1] + " and signZ = " + condition[2] + " and level = '" + condition[3] + "'");
            if(rs.getString("shopOwner").equals(player.getName())) return true;
        } catch(SQLException e) {
            System.err.println(e.getMessage() + " : at isShopOwner");
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                	System.out.println(e.getMessage() + " : at isShopOwner on close connection");
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
    
    public boolean existsShop(Object[] condition) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + this.path + "/DataDB.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);

            ResultSet rs = statement.executeQuery("select * from ChestShop where signX = " + condition[0] + " and signY = " + condition[1] + " and signZ = " + condition[2] + " and level = '" + condition[3] + "'");
            return rs.next();
        } catch(SQLException e) {
            System.err.println(e.getMessage() + " : at existsShop");
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                	System.out.println(e.getMessage() + " : at existsShop on close connection");
                    e.printStackTrace();
                }
            }
        }
        return false;
    }
    
    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
        }catch(Exception e){
            System.err.println(e.getMessage() + " : at connect");
        }
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + this.path + "/DataDB.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30);
            
            statement.executeUpdate("create table if not exists ChestShop " +
                    "(" +
                    "id integer primary key autoincrement, " +
                    "shopOwner text not null, " +
                    "saleNum integer not null, " +
                    "price integer not null, " +
                    "productID integer not null, " +
                    "productMeta integer not null, " +
                    "signX integer not null, " +
                    "signY integer not null, " +
                    "signZ integer not null, " +
                    "chestX integer not null, " +
                    "chestY integer not null, " +
                    "chestZ integer not null," +
                    "level text not null" +
                    ")"
            );
        } catch(SQLException e) {
            System.err.println(e.getMessage() + " : at createDB");
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.err.println(e.getMessage() + " : at createDB connection close");
                }
            }
        }
    }

}
