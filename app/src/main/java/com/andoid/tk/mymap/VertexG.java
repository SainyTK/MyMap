package com.andoid.tk.mymap;

/**
 * Created by TK on 4/5/2018.
 */

public class VertexG
{
    private int name;
    private int[] toOther;
    private boolean[] visited;

    public VertexG(int name,int numberOfNode)
    {
        this.name = name;
        this.toOther = new int[numberOfNode];
        this.visited = new boolean[numberOfNode];
        for(int i = 0;i<numberOfNode;i++)
        {
            this.toOther[i] = Integer.MAX_VALUE;
            this.visited[i] = false;
        }
    }

    public int getName()
    {
        return this.name;
    }

    public boolean getVisited(int n)
    {
        return this.visited[n];
    }

    public void setVisited(int n, boolean visited)
    {
        this.visited[n] = visited;
    }

    public void setDistance(int name,int disance)
    {
        if(this.name != name)
        {
            this.toOther[name] = disance;
        }
    }

    public int getDistance(int name)
    {
        return toOther[name];
    }

    public void connectTo(VertexG v,int disance)
    {
        if(this.name != v.getName())
        {
            this.toOther[v.getName()] = disance;
            v.setDistance(this.name,disance);
        }
    }

    public int getDistance(VertexG v)
    {
        return this.toOther[v.getName()];
    }

    public int findMinI()
    {
        int i,min = name;
        for(i=0;i<toOther.length;i++)
        {
            if(i!=name&&visited[i]==false&&this.toOther[i]<this.toOther[min])
            {
                min = i;
            }
        }
        return min;
    }

    public int findMinV()
    {
        int min = name;
        for(int i=0;i<toOther.length;i++)
        {
            if(i!=name&&visited[i]==false&&this.toOther[i]<this.toOther[min])
                min = i;
        }
        return this.toOther[min];
    }

    public void showAllPaths()
    {
        System.out.print((this.name+1) + " : ");
        for(int i=0;i<this.toOther.length;i++)
        {
            if(this.toOther[i]>=Integer.MAX_VALUE/1000)
                System.out.printf("\t-");
            else
                System.out.printf("\t%d",this.toOther[i]);
        }
        System.out.println();
    }
}

