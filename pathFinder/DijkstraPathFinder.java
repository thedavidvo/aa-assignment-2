package pathFinder;

import map.Coordinate;
import map.PathMap;

import java.util.*;

public class DijkstraPathFinder implements PathFinder
{


	private PathMap map;
	private HashMap<Coordinate, Double> unvisited = new HashMap<Coordinate, Double>();
	private HashMap<Coordinate, Coordinate> visited = new HashMap<Coordinate, Coordinate>();
	private Coordinate[][] cells;

	private double infinity = Double.POSITIVE_INFINITY;

	private int count = 0;

	public DijkstraPathFinder(PathMap map) 
	{   
		//initalising the map and variables when the object is made (for easy access).
		this.map = map;
		cells = map.getCell();

		//generating surroundings for each coordinate.
		for(int i = 0; i < map.sizeR; i++) 
		{
			for (int j = 0; j < map.sizeC; j++) 
			{
				cells[j][i].upCoordinate(map);
				cells[j][i].rightCoordinate(map);
				cells[j][i].downCoordinate(map);
				cells[j][i].leftCoordinate(map);
			}
		}

		//setting all unreachable coordinates to infinity
		resetMap();
	}
	@Override
	public List<Coordinate> findPath() 
	{
		ArrayList<Coordinate> path = new ArrayList<Coordinate>();
		double cheapestTotCost = Double.POSITIVE_INFINITY;
		List<Coordinate> waypCopy = new ArrayList<Coordinate>();

		//deep copy of the wayPoint for comparisons in the algorithm.
		waypCopy = copyList(map.getWayPoint());

		double cost = Double.POSITIVE_INFINITY;

		//Populating the source nodes
		for(int i = 0; i < map.getOrigin().size(); i++)
		{	
			map.getOrigin().get(i).setPreviousCo(map.getOrigin().get(i));
			unvisited.put(map.getOrigin().get(i), (double) 0);
			map.getOrigin().get(i).setCostToCoordinate(0);

			ArrayList<Coordinate> tempPath = new ArrayList<Coordinate>();

			waypCopy = copyList(map.getWayPoint());

			Coordinate checkpoint = map.getOrigin().get(i);

			double costTot = 0;

			while(!waypCopy.isEmpty())
			{

				for(int m = 0; m < waypCopy.size(); m++) 
				{
					//Map needs to reset each time we start from a new source node
					resetMap();

					//When we iterate at a new source node, we need to prioritise the source node for checking
					checkpoint.setPreviousCo(map.getWayPoint().get(m));
					unvisited.put(checkpoint, (double) 0);
					checkpoint.setCostToCoordinate(0);	
					cost = Double.POSITIVE_INFINITY;

					//Will continuously loop until the unvisited list is empty to find the shortest path to each node, this is for comparison
					while(!unvisited.isEmpty())
					{			
						Coordinate smallestCoordinate = performAlg();
						
						if(waypCopy.contains(smallestCoordinate))
						{
							if( smallestCoordinate.getCostToCoordinate() < cost)
							{
								cost = smallestCoordinate.getCostToCoordinate();

								ArrayList<Coordinate> temp = findPath(checkpoint,smallestCoordinate, this.visited);
								costTot += cost;

								for (int tr = 0; tr < temp.size() - 1; tr++) 
								{
									tempPath.add(temp.get(tr));
								}
								checkpoint = smallestCoordinate;
							}
						}	
					}
					waypCopy.remove(checkpoint);
				}
			}
			resetMap();

			//When we iterate at a new source node, we need to prioritise the source node for checking
			checkpoint.setPreviousCo(checkpoint);
			unvisited.put(checkpoint, (double) 0);
			checkpoint.setCostToCoordinate(0);	
			cost = Double.POSITIVE_INFINITY;

			//Will continuously loop until the unvisited list is empty to find the shortest path to each node, this is for comparison
			while(!unvisited.isEmpty())
			{			
				Coordinate smallestCoordinate = performAlg();
				
				//If we hit the destination node, and it's cheaper than the previous destination costs, then we use that as our final path
				if(map.getDestination().contains(smallestCoordinate)) 
				{	 
					if( smallestCoordinate.getCostToCoordinate() < cost)
					{
						cost = smallestCoordinate.getCostToCoordinate();

						ArrayList<Coordinate> temp = findPath(checkpoint,smallestCoordinate, this.visited);

						costTot += cost;

						for (int tr = 0; tr < temp.size(); tr++) 
						{
							tempPath.add(temp.get(tr));
						}
					}
				}
			}

			//final comparison between the paths from each origin through each checkpoint to the destination
			if( costTot < cheapestTotCost)
			{
				cheapestTotCost = costTot;

				path = tempPath;
			}			
		}

		count = path.size();

		return path;
	} 

	public Coordinate performAlg() 
	{
		Coordinate smallestCoordinate = null;

		//If the coordinate is added to visited, we have access to it's neighboring nodes and can update if it is cheaper
		for(Coordinate key: visited.keySet())
		{
			updateSurroundingNodes(key);
		}

		smallestCoordinate = null;

		//Settings the weight as infinity as the there could be high numbered weights
		double smallestWeight = Double.POSITIVE_INFINITY;

		//Reiterate in our unvisited set, and pick the coordinate with the smallest value
		for(Coordinate coordinate: unvisited.keySet()) 
		{
			//Selecting the cheapest path and adding to visited, as we can ensure 100% that it is the cheapest cost
			if(unvisited.get(coordinate) < smallestWeight) 
			{
				smallestWeight = unvisited.get(coordinate);
				smallestCoordinate = coordinate;
			}
		}

		//Adding that coordinate to the list as well as it's previous coordinate for path finding
		visited.put(smallestCoordinate, smallestCoordinate.getPreviousCo());
		unvisited.remove(smallestCoordinate);

		return smallestCoordinate;
	}

	public List<Coordinate> copyList(List<Coordinate> want2copy){

		//this method returns a deep copy of the desired list
		List<Coordinate> temp = new ArrayList<Coordinate>();

		for(Coordinate wa : want2copy) 
		{
			Coordinate newCo = new Coordinate();
			newCo = wa;
			temp.add(newCo);
		}

		return temp;
	}


	public ArrayList<Coordinate> findPath(Coordinate start, Coordinate end, HashMap<Coordinate, Coordinate> visited)
	{

		//When we print the list of coordinates, we start from the end and make our way to the start
		boolean foundStart = false;
		Coordinate temp = end;
		ArrayList<Coordinate> path = new ArrayList<Coordinate>();

		while(!foundStart)
		{ 			
			for (Coordinate co : visited.keySet()) 
			{
				//Condition to find start node
				if (temp.equals(start)) 
				{						
					path.add(start);
					foundStart = true;
					break;
				}

				//Condition to find nodes in between
				else if (temp.getPreviousCo()!= null && temp.equals(co)) 
				{
					path.add(co);
					temp = co.getPreviousCo();
					break;
				}

				//Condition to find the end node
				else if(temp.equals(co))
				{
					path.add(co);
					temp = co.getPreviousCo();
					break;
				}
			}
		}

		//We reverse the collection as we want to read it in order
		Collections.reverse(path);
		return path;
	}		

	public void resetMap()
	{ 
		unvisited.clear();
		//Populating the map for checking
		for(int i = 0; i < map.getRow(); i++)
		{
			for( int j = 0; j < map.getCol(); j++)
			{
				//Adding the nodes that are passable into the unvisited arraylist for checking
				if(!cells[j][i].getImpassable()) 
				{
					cells[j][i].setCostToCoordinate(0);
					unvisited.put(cells[j][i], infinity);
				}
			}
		}

		visited.clear();
	}



	//Method for checking a coordinates surrounding and to replace it's value if it is cheaper than it's previous cost.
	public void updateSurroundingNodes(Coordinate c)
	{
		for(int i = 1; i <= 4; i++) 
		{			
			if(c.getSurroundings().get(i) != null) 
			{
				//up
				if(i == 1)
				{
					if(c.getSurroundings().get(i)!=null && unvisited.containsKey(c.getSurroundings().get(i)))
					{
						if(cells[c.getRow()+1][c.getColumn()].getTerrainCost() + c.getCostToCoordinate() < unvisited.get(c.getSurroundings().get(i)))
						{	
							double totalCost = cells[c.getRow()+1][c.getColumn()].getTerrainCost() + c.getCostToCoordinate();

							unvisited.put(cells[c.getRow()+1][c.getColumn()], totalCost);
							cells[c.getRow()+1][c.getColumn()].setCostToCoordinate(totalCost);
							cells[c.getRow()+1][c.getColumn()].setPreviousCo(c);
						}
					}
				} 

				//right
				if(i == 2)
				{
					if(unvisited.containsKey(c.getSurroundings().get(i)) && c.getSurroundings().get(i)!=null)
					{
						if(cells[c.getRow()][c.getColumn()+1].getTerrainCost() + c.getCostToCoordinate() < unvisited.get(c.getSurroundings().get(i)))
						{	
							double totalCost = cells[c.getRow()][c.getColumn()+1].getTerrainCost() + c.getCostToCoordinate();

							unvisited.put(cells[c.getRow()][c.getColumn()+1], totalCost);
							cells[c.getRow()][c.getColumn()+1].setCostToCoordinate(totalCost);
							cells[c.getRow()][c.getColumn()+1].setPreviousCo(c);
						}
					}
				} 

				//down
				if(i == 3)
				{
					if(unvisited.containsKey(c.getSurroundings().get(i)) && c.getSurroundings().get(i)!=null)
					{
						if(cells[c.getRow()-1][c.getColumn()].getTerrainCost() + c.getCostToCoordinate() < unvisited.get(c.getSurroundings().get(i)))
						{	
							double totalCost = cells[c.getRow()-1][c.getColumn()].getTerrainCost() + c.getCostToCoordinate();

							unvisited.put(cells[c.getRow()-1][c.getColumn()], totalCost);
							cells[c.getRow()-1][c.getColumn()].setCostToCoordinate(totalCost);
							cells[c.getRow()-1][c.getColumn()].setPreviousCo(c);
						}
					}
				} 

				//left
				if(i == 4)
				{
					if(unvisited.containsKey(c.getSurroundings().get(i)) && c.getSurroundings().get(i)!=null)
					{
						if(cells[c.getRow()][c.getColumn()-1].getTerrainCost() + c.getCostToCoordinate() < unvisited.get(c.getSurroundings().get(i)))
						{	
							double totalCost = cells[c.getRow()][c.getColumn()-1].getTerrainCost() + c.getCostToCoordinate();

							unvisited.put(cells[c.getRow()][c.getColumn()-1], totalCost);
							cells[c.getRow()][c.getColumn()-1].setCostToCoordinate(totalCost);
							cells[c.getRow()][c.getColumn()-1].setPreviousCo(c);
						}
					}
				} 
			}
		}
	}

	//number of coordinates visited including itself
	//(optional but I hope you like itt)
	public int coordinatesExplored() 
	{
		return count;
	} 
} // end of class DijsktraPathFinder
