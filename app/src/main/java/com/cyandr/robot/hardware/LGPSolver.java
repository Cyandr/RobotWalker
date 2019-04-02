package com.cyandr.robot.hardware;

import java.util.*;

public class LGPSolver {

    private LGPSDesk m_desk;
    private List<DeskPoint> m_knownForceList;
    private DeskPoint m_curMesureForce;

    LGPSolver(LGPSDesk lgpsDesk) {

        m_desk = lgpsDesk;
        m_knownForceList = new ArrayList<>();
        m_curMesureForce = new DeskPoint();
    }

    public static DeskPoint buildDeskPoint() {
        DeskPoint point = new DeskPoint();
        return point;
    }

    public DeskPoint getResult() {


        return m_curMesureForce;


    }

    boolean setDataFromSensorArray(List<Float> sensorValues) {

        List<DeskPoint> deskValues = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            DeskPoint point = new DeskPoint();
            point.posX = m_desk.width - m_desk.legoffsetX[i];
            point.posY = m_desk.height - m_desk.legoffsetY[i];
            point.force = sensorValues.get(i);
            deskValues.add(point);
        }
        return setMeasureData(deskValues);
    }

    private boolean setMeasureData(List<DeskPoint> pointforces) {
        if (pointforces.size() < 3) {
            return false;
        }
        m_knownForceList.addAll(pointforces);
        m_curMesureForce.force = 0;
        for (DeskPoint point : pointforces) {
            m_curMesureForce.force += point.force;
        }
        return true;
    }

    boolean Solve() {
        List<Float> momentSum = new ArrayList<>();
        //对m_knownForceList 的第一个元素取力矩
        for (int i = 0; i < m_knownForceList.size(); i++) {
            DeskPoint pointI = m_knownForceList.get(i);
            float curMomentSum = 0;
            for (int j = 0; j < m_knownForceList.size(); j++) {
                if (i == j) continue;
                DeskPoint pointJ = m_knownForceList.get(i);
                //要测量点距每个已知点的距离
                curMomentSum += pointJ.force * pointJ.getLength(pointI) / m_curMesureForce.force;

            }

            momentSum.add(curMomentSum);
        }
        solveEquation(momentSum);
        return true;
    }

    void solveEquation(List<Float> distances) {


        List<DeskPoint> listRadius = new ArrayList<>();
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0, j = 0; i < m_knownForceList.size(); i++, j = i + 1) {
            if (i == m_knownForceList.size() - 1)
                j = 0;
            DeskPoint pointI = m_knownForceList.get(i);
            DeskPoint pointJ = m_knownForceList.get(j);
            float r1 = distances.get(i);
            float r2 = distances.get(j);

            List<DeskPoint> ptIntersects = intersect(pointI.posX, pointI.posY, pointJ.posX, pointJ.posY, r1, r2);
            if (ptIntersects != null) {
                listRadius.addAll(ptIntersects);
            }
        }

        if (listRadius.size() == 0)
            return;
        for (int i = 0; i < listRadius.size(); i++) {
            DeskPoint point = listRadius.get(i);
            int num = 1;
            for (int j = 0; j < listRadius.size(); j++) {
                if (i == j) continue;
                DeskPoint pointB = listRadius.get(j);
                if (pointB.getLength(point) < 50)
                    num++;
            }
            map.put(i, num);
        }
        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(map.entrySet());
        //然后通过比较器来实现排序
        Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
            //升序排序
            public int compare(Map.Entry<Integer, Integer> o1,
                               Map.Entry<Integer, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }

        });

        Map.Entry<Integer, Integer> biggrst = list.get(list.size() - 1);
        int index = biggrst.getKey();
        DeskPoint point = listRadius.get(index);
        m_curMesureForce.posX = point.posX;
        m_curMesureForce.posY = point.posY;


    }

    /**
     * 求相交
     *
     * @return {x1 , y1 , x2 , y2}
     */
    private List<DeskPoint> intersect(float x1, float x2, float y1, float y2, float r1, float r2) {

        // 在一元二次方程中 a*x^2+b*x+c=0
        double a, b, c;

        //x的两个根 x_1 , x_2
        //y的两个根 y_1 , y_2
        double x_1 = 0, x_2 = 0, y_1 = 0, y_2 = 0;
        DeskPoint deskPointI = new DeskPoint();
        DeskPoint deskPointJ = new DeskPoint();
        //判别式的值
        double delta = -1;

        //如果 y1!=y2
        if (y1 != y2) {

            //为了方便代入
            double A = (x1 * x1 - x2 * x2 + y1 * y1 - y2 * y2 + r2 * r2 - r1 * r1) / (2 * (y1 - y2));
            double B = (x1 - x2) / (y1 - y2);

            a = 1 + B * B;
            b = -2 * (x1 + (A - y1) * B);
            c = x1 * x1 + (A - y1) * (A - y1) - r1 * r1;

            //下面使用判定式 判断是否有解
            delta = b * b - 4 * a * c;

            if (delta > 0) {

                x_1 = (-b + Math.sqrt(b * b - 4 * a * c)) / (2 * a);
                x_2 = (-b - Math.sqrt(b * b - 4 * a * c)) / (2 * a);
                y_1 = A - B * x_1;
                y_2 = A - B * x_2;
            } else if (delta == 0) {
                x_1 = x_2 = -b / (2 * a);
                y_1 = y_2 = A - B * x_1;
            } else {
                System.err.println("两个圆不相交");
                return null;
            }
        } else if (x1 != x2) {

            //当y1=y2时，x的两个解相等
            x_1 = x_2 = (x1 * x1 - x2 * x2 + r2 * r2 - r1 * r1) / (2 * (x1 - x2));

            a = 1;
            b = -2 * y1;
            c = y1 * y1 - r1 * r1 + (x_1 - x1) * (x_1 - x1);

            delta = b * b - 4 * a * c;

            if (delta > 0) {
                y_1 = (-b + Math.sqrt(b * b - 4 * a * c)) / (2 * a);
                y_2 = (-b - Math.sqrt(b * b - 4 * a * c)) / (2 * a);
            } else if (delta == 0) {
                y_1 = y_2 = -b / (2 * a);
            } else {
                System.err.println("两个圆不相交");
                return null;
            }
        } else {
            System.out.println("无解");
            return null;
        }
        deskPointI.posX = (float) x_1;
        deskPointI.posY = (float) y_1;
        deskPointJ.posX = (float) x_2;
        deskPointJ.posY = (float) y_2;
        List<DeskPoint> arrpt = new ArrayList<>();

        arrpt.add(deskPointI);
        arrpt.add(deskPointJ);

        return arrpt;
    }

    public static class LGPSDesk {

        public int width;
        public int height;
        public int[] legoffsetX = {0, 0, 0, 0};
        public int[] legoffsetY = {0, 0, 0, 0};

        public LGPSDesk(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public void setOffx(int[] offx) {
            if (offx.length == 4) {
                legoffsetX = offx;
            }
        }

        public void setOffy(int[] offy) {
            if (offy.length == 4) {
                legoffsetY = offy;
            }
        }
    }

    public static class DeskPoint {

        public float posX;
        public float posY;
        public float force;

        double getLength(DeskPoint pointinDeskp) {

            return Math.sqrt((pointinDeskp.posX - this.posX) * (pointinDeskp.posX - this.posX) +
                    (pointinDeskp.posY - this.posY) * (pointinDeskp.posY - this.posY));

        }

    }

}
