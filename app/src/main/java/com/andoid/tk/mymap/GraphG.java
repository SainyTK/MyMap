package com.andoid.tk.mymap;

import java.util.ArrayList;

/**
 * Created by TK on 4/5/2018.
 */

public class GraphG
{
    private ArrayList<VertexG> vertices;
    private int[] path;
    private int step;
    private int[] distance;

    public GraphG(int numberOfNode)
    {
        this.vertices = new ArrayList<VertexG>();
        this.path = new int[numberOfNode+1];
        this.distance = new int[numberOfNode+1];
        this.step = 0;
        for(int i=0;i<numberOfNode;i++)
        {
            VertexG v = new VertexG(i,numberOfNode);
            this.vertices.add(v);
        }
    }

    public void add(VertexG v)
    {
        this.vertices.add(v);
    }

    public void connect(int n1,int n2,int distance)
    {
        if(n1<vertices.size()&&n2<vertices.size())
        {
            vertices.get(n1).connectTo(vertices.get(n2),distance);
        }
    }


    public void TSPNear(int start)
    {
        int i,src=0,dst=0;
        beginTravel(start);
        while(step<vertices.size())
        {
            dst = vertices.get(path[step-1]).findMinI();
            travelTo(dst);
            src = dst;
        }
        travelTo(start);
    }

    public void beginTravel(int node)
    {
        for(int i=0;i<vertices.size();i++)
            vertices.get(i).setVisited(node,true);
        path[0] = node;
        step++;
    }

    public void travelTo(int node)
    {
        distance[step] = vertices.get(path[step-1]).getDistance(vertices.get(node));
        for(int i=0;i<vertices.size();i++)
            vertices.get(i).setVisited(node,true);
        path[step] = node;
        step++;
    }

    public void showAllNodes()
    {
        System.out.println("-------------------------------------------------------------------------");
        for (int i=0;i<vertices.size();i++ )
        {
            System.out.printf("\t%d",i+1);
        }
        System.out.printf("\n");
        for(VertexG v:vertices)
        {
            v.showAllPaths();
        }
        System.out.println("---------------------------------------------------------------------------");
    }

    public void showPath()
    {
        System.out.printf("Path : ");
        int i,totalDistance = 0;;
        for(i=0;i<step-1;i++)
        {
            System.out.printf("%d -> ",path[i]+1);
        }
        System.out.println(path[i]+1);
        for(i=0;i<step-1;i++)
        {
            System.out.printf("%d + ",distance[i]);
            totalDistance += distance[i];
        }
        totalDistance += distance[i];
        System.out.println(distance[i]);
        System.out.printf("Total Distance : %d\n",totalDistance);
    }

    public int[] getPath()
    {
        return path;
    }
}
