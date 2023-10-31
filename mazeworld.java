import java.awt.Color;
import java.util.*;
import javalib.impworld.*;
import javalib.worldimages.*;
import tester.*;

// represent a vertex class
class Vertex {

  int x; 
  int y;
  Posn position;
  boolean isPathVertex = false;
  boolean isFindVertex = false;

  ArrayList<Edge> outter;

  // vertex constructor
  Vertex(Posn position, ArrayList<Edge> outter) {
    this.position = position;
    this.outter = outter;
    this.isPathVertex = false;
    this.isFindVertex = false;
  }

  // determines if two vertexes are equal
  @ Override
  public boolean equals(Object o) {
    if (!(o instanceof Vertex)) {
      return false;
    }
    Vertex other = (Vertex) o;
    return (this.position.x == other.position.x && this.position.y == other.position.y);
  }

  // overrides hashcode
  public int hashCode() {
    return this.position.hashCode() * 1000;
  }

  // draw a single cell
  WorldImage drawCell() {
    return new RectangleImage(MazeWorld.CELL_SIZE, MazeWorld.CELL_SIZE, 
        OutlineMode.SOLID, Color.LIGHT_GRAY);
  }

  // draw a cell of a find vertex
  WorldImage drawFindVertex() {
    if (this.isFindVertex) {
      return new RectangleImage(MazeWorld.CELL_SIZE, MazeWorld.CELL_SIZE, 
          OutlineMode.SOLID, Color.yellow);
    }
    else {
      return new EmptyImage();
    }
  }

  // draw a cell of a vertex in the final  path
  WorldImage drawPathVertex() {
    if (this.isPathVertex) {
      return new RectangleImage(MazeWorld.CELL_SIZE, MazeWorld.CELL_SIZE, 
          OutlineMode.SOLID, Color.cyan);
    }
    else {
      return new EmptyImage();
    }
  }

  // draw a playerVertex
  WorldImage drawPlayerVertex() {
    return new RectangleImage(MazeWorld.CELL_SIZE, MazeWorld.CELL_SIZE, 
        OutlineMode.SOLID, Color.pink);
  }
}

// represent a Edge class
class Edge implements Comparable<Edge> {
  Vertex from;
  Vertex to;
  int weight;

  // edge constructor
  Edge(Vertex from, Vertex to, int weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
  }

  // determines if two edges are equal
  @ Override
  public boolean equals(Object o) {
    if (!(o instanceof Edge)) {
      return false;
    }
    Edge other = (Edge) o;
    return (this.from.equals(other.from) || this.from.equals(other.to))
        && (this.to.equals(other.to) || this.to.equals(other.from));
  }

  // determined if this weight is smaller than the given edge
  public int compareTo(Edge edge) {
    if (this.weight < edge.weight) {
      return -1;
    }
    if (this.weight > edge.weight) {
      return 1;
    }
    else {
      return 0;
    }
  }

  // draw a edge
  WorldImage drawEdge() {
    return new RectangleImage(MazeWorld.CELL_SIZE, MazeWorld.CELL_SIZE, 
        OutlineMode.SOLID, Color.LIGHT_GRAY);
  }
}

// represent a player class
class Player {
  int x;
  int y;

  // player constructor
  Player(int x, int y) {
    this.x = x;
    this.y = y;
  }

  // draw the player
  WorldImage drawPlayer() {
    return new RectangleImage(MazeWorld.CELL_SIZE, MazeWorld.CELL_SIZE, 
        OutlineMode.SOLID, Color.yellow);
  }
}

// represent a MazeWorld Class
class MazeWorld extends World {

  static final int HEIGHT = 15;
  static final int WIDTH = 20;
  static final int CELL_SIZE = 9;

  ArrayList<ArrayList<Vertex>> board;
  HashMap<Vertex, Vertex> representatives = new HashMap<Vertex, Vertex>();
  List<Edge> edgesInTree;

  // edges that sorted by edge weights
  List<Edge> worklist;

  // a list of find vertices which shows the process of finding the final path
  ArrayList<Vertex> find;

  // a list of vertices that are in the final path
  ArrayList<Vertex> finalPath;

  Player player;
  int numPress;
  double tickRate = 0.1;
  int count;
  int move = 0;
  int wrongMove = 0;
  double time = 0.0;
  boolean worldEnd = false;
  ArrayList<Vertex> playerPath;
  boolean colorPlayerPath;

  // maze world constructor
  MazeWorld() {
    this.board = this.makeBoard();
    this.edgesInTree = new ArrayList<Edge>();
    this.worklist = this.sortEdges(this.makeEdges());
    this.minSpanningTree(this.worklist, this.board);
    this.assignEdges();
    // seen
    this.find = new ArrayList<Vertex>();
    // solution
    this.finalPath = new ArrayList<Vertex>();

    this.player = new Player(0, 0);
    this.numPress = 0;
    this.tickRate = 0.1;
    this.count = -1;
    this.wrongMove = 0;
    this.worldEnd = false;
    this.playerPath = new ArrayList<Vertex>(Arrays.asList(this.board.get(0).get(0)));
    this.colorPlayerPath = true;
  }

  // make a two-dimensional board using the height and width of maze world
  // where there are no edges between the cells
  ArrayList<ArrayList<Vertex>> makeBoard() {
    ArrayList<ArrayList<Vertex>> board = new ArrayList<ArrayList<Vertex>>();
    for (int i = 0; i < MazeWorld.WIDTH; i++) {
      ArrayList<Vertex> rowBoard = new ArrayList<Vertex>();
      for (int j = 0; j < MazeWorld.HEIGHT; j++) {
        ArrayList<Edge> e = new ArrayList<Edge>();
        Vertex v = new Vertex(new Posn(i, j), e);
        rowBoard.add(v);
      }
      board.add(rowBoard);
    }
    return board;
  }

  // make edges between the cells inside the board
  ArrayList<Edge> makeEdges() {
    ArrayList<Edge> edge = new ArrayList<Edge>();
    for (int i = 0; i < this.board.size(); i++) {
      for (int j = 0; j < this.board.get(i).size(); j++) {

        // now in rightmost col , add edge to right vertex
        if (i < MazeWorld.WIDTH - 1) {
          Vertex vertex = board.get(i).get(j);
          Vertex right = board.get(i + 1).get(j);
          Edge edge1 = new Edge(vertex, right, new Random().nextInt());
          edge.add(edge1);
        }

        // if not in bottom j, add edge to bottom vertex
        if (j < MazeWorld.HEIGHT - 1) {
          Vertex vertex = board.get(i).get(j);
          Vertex bottom = board.get(i).get(j + 1);
          Edge edge2 = new Edge(vertex, bottom, new Random().nextInt());
          edge.add(edge2);
        }
      }
    }
    return edge;
  }

  // sort a list of edges by weight
  List<Edge> sortEdges(ArrayList<Edge> edges) {
    Collections.sort(edges);
    return edges;
  }

  // makes each vertex a representative of itself first and then connects the
  // vertices
  List<Edge> minSpanningTree(List<Edge> worklist, ArrayList<ArrayList<Vertex>> board) {
    HashMap<Vertex, Vertex> rep = new HashMap<Vertex, Vertex>();
    for (ArrayList<Vertex> lov : board) {
      for (Vertex v : lov) {
        rep.put(v, v);
      }
    }
    while (this.edgesInTree.size() < rep.size() - 1) {

      Edge curr = worklist.remove(0);
      if (!find(rep, curr.to).equals(find(rep, curr.from))) {
        this.edgesInTree.add(curr);
        union(rep, find(rep, curr.to), (find(rep, curr.from)));
      }
    }
    worklist.removeAll(this.edgesInTree);
    this.representatives = rep;
    return this.edgesInTree;
  }

  // to find the representative of the given cell in the HashMap
  Vertex find(HashMap<Vertex, Vertex> rep, Vertex v) {
    if (rep.get(v).equals(v)) {
      return v;
    }
    else {
      return find(rep, rep.get(v));
    }
  }

  // EFFECT: to connect one representative to the other representative
  void union(HashMap<Vertex, Vertex> rep, Vertex v1, Vertex v2) {
    rep.put(v1, rep.get(v2));
  }

  // assigns the edges in edgesInTree to the vertices in the board
  void assignEdges() {
    for (Edge edge : this.edgesInTree) {
      edge.from.outter.add(edge);
      edge.to.outter.add(edge);
    }
  }

  // draw the Maze on the World Canvas
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(MazeWorld.WIDTH, MazeWorld.HEIGHT);
    // draw the board
    for (ArrayList<Vertex> vertex : this.board) {
      for (Vertex v : vertex) {
        scene.placeImageXY(v.drawCell(), v.position.x * 10 + 5, v.position.y * 10 + 5);
      }
    }
    // draw the edges
    for (Edge e : this.edgesInTree) {
      scene.placeImageXY(e.drawEdge(), (e.to.position.x + e.from.position.x) * 5 + 5,
          (e.to.position.y + e.from.position.y) * 5 + 5);
    }
    // draw the end point
    scene.placeImageXY(new RectangleImage(9, 9, OutlineMode.SOLID, Color.green),
        MazeWorld.WIDTH * 10 - 5, MazeWorld.HEIGHT * 10 - 5);

    // draw the find vertex
    for (Vertex v : this.find) {
      scene.placeImageXY(v.drawFindVertex(), v.position.x * 10 + 5, v.position.y * 10 + 5);
    }
    // draw the final path
    if (this.count >= this.find.size()) {
      for (Vertex v : this.finalPath) {
        scene.placeImageXY(v.drawPathVertex(), v.position.x * 10 + 5, v.position.y * 10 + 5);
      }
    }
    // draw the player's path
    if (this.colorPlayerPath) {
      for (Vertex v : this.playerPath) {
        scene.placeImageXY(v.drawPlayerVertex(), v.position.x * 10 + 5, v.position.y * 10 + 5);
      }
    }
    // draw the player vertex
    scene.placeImageXY(this.player.drawPlayer(), player.x * 10 + 5, player.y * 10 + 5);

    // draw the timer on the board
    scene.placeImageXY(new TextImage("Time: " + time + "s", 
        MazeWorld.WIDTH * MazeWorld.HEIGHT / 25, Color.black), 
        MazeWorld.WIDTH * MazeWorld.CELL_SIZE - 15, 10);
    //MazeWorld.WIDTH * MazeWorld.CELL_SIZE / 2, (MazeWorld.CELL_SIZE + 2) * MazeWorld.HEIGHT);

    // draw the player moves on the board
    scene.placeImageXY(new TextImage("Moves: " + move, 
        MazeWorld.WIDTH * MazeWorld.HEIGHT / 25, Color.black), 
        MazeWorld.WIDTH * MazeWorld.CELL_SIZE - 15, 25);

    // draw the title of the maze game
    scene.placeImageXY(new TextImage("Maze Game", 
        MazeWorld.WIDTH * MazeWorld.HEIGHT / 15, Color.black), 
        MazeWorld.WIDTH * MazeWorld.CELL_SIZE / 2, (MazeWorld.CELL_SIZE + 2) * MazeWorld.HEIGHT);

    // draw the final scene 
    WorldImage wrongMoves = (new TextImage("Wrong Moves: " + this.wrongMove, Color.black));
    WorldImage congrats = new AboveImage(new TextImage("Congratulation! Path Found!", Color.black));
    WorldImage finalscene = new AboveImage(congrats, wrongMoves);

    // displays the final scene if the player reaches the end point of the maze game
    if (this.player.x == this.board.get(MazeWorld.WIDTH - 1).get(MazeWorld.HEIGHT - 1).position.x
        && this.player.y == this.board.get(MazeWorld.WIDTH - 1)
        .get(MazeWorld.HEIGHT - 1).position.y) {
      this.tickRate = 0;
      this.worldEnd = true;
      scene.placeImageXY(
          congrats,
          MazeWorld.WIDTH * 10 / 2, MazeWorld.HEIGHT * 10 / 2);
    }

    if (this.board.get(0).get(0).isPathVertex) {
      this.tickRate = 0;
      scene.placeImageXY(
          finalscene,
          MazeWorld.WIDTH * 10 / 2, MazeWorld.HEIGHT * 10 / 2);
    }
    return scene;
  }

  // check if there is a path from this position to another position
  boolean isPath(int toX, int toY, int fromX, int fromY) {
    return this.edgesInTree.contains(new Edge(new Vertex(new Posn(toX, toY), new ArrayList<Edge>()),
        new Vertex(new Posn(fromX, fromY), new ArrayList<Edge>()), 0));
  }

  // moves the player by pressing "up" "down" "right" "left" key
  public void onKeyEvent(String k) {
    if (k.equals("up")) {
      if (this.isPath(this.player.x, this.player.y - 1, this.player.x, this.player.y)) {
        this.player.y = this.player.y - 1;
        this.playerPath.add(this.board.get(this.player.x).get(this.player.y));
        this.move++;
        this.numPress++;
      }
    }
    if (k.equals("down")) {
      if (this.isPath(this.player.x, this.player.y + 1, this.player.x, this.player.y)) {
        this.player.y = this.player.y + 1;
        this.playerPath.add(this.board.get(this.player.x).get(this.player.y));
        this.move++;
        this.numPress++;
      }
    }
    if (k.equals("left")) {
      if (this.isPath(this.player.x - 1, this.player.y, this.player.x, this.player.y)) {
        this.player.x = this.player.x - 1;
        this.playerPath.add(this.board.get(this.player.x).get(this.player.y));
        this.move++;
        this.numPress++;
      }
    }
    if (k.equals("right")) {
      if (this.isPath(this.player.x + 1, this.player.y, this.player.x, this.player.y)) {
        this.player.x = this.player.x + 1;
        this.playerPath.add(this.board.get(this.player.x).get(this.player.y));
        this.move++;
        this.numPress++;
      }
    }
    // press "b" to manipulate breath-first search
    if (k.equals("b")) {
      this.count = 0;
      this.findPath(k);
      this.score(this.find, this.finalPath);
      this.move = this.find.size(); 
      this.numPress++;
    }
    // press "d" to manipulate depth-first search
    if (k.equals("d")) {
      this.count = 0;
      this.findPath(k);
      this.score(this.find, this.finalPath);
      this.move = this.find.size(); 
      this.numPress++;
    }
    // whistle: press "r" to reset the maze world game
    if (k.equals("r")) {
      MazeWorld restart = new MazeWorld();
      this.time = 0;
      this.move = 0;
      this.numPress = 0;
      this.board = restart.board;
      this.representatives = restart.representatives;
      this.edgesInTree = restart.edgesInTree;
      this.worklist = restart.worklist;
      this.player = restart.player;
      this.find = restart.find;
      this.finalPath = restart.finalPath;
      this.count = restart.count;
      this.playerPath = restart.playerPath;
    }
    // press "c" to shows and hide the player's path 
    if (k.equals("c")) {
      this.colorPlayerPath = !this.colorPlayerPath;
    }
  }

  // find the path of using breadth first search or depth first search
  void findPath(String k) {
    HashMap<Vertex, Vertex> path = new HashMap<Vertex, Vertex>();
    ArrayList<Vertex> worklist = new ArrayList<Vertex>();
    worklist.add(this.board.get(0).get(0));
    this.find.clear();

    while (worklist.size() > 0) {
      Vertex next = worklist.remove(0);
      Vertex finalNode = this.board.get(this.board.size() - 1).get(this.board.get(0).size() - 1);
      if (next.equals(finalNode)) {
        this.reconstruct(path, next);
        this.score(find, finalPath);
        this.worldEnd = true;
        return;
      }
      for (Edge e : next.outter) {
        if (!this.find.contains(e.to) && next.equals(e.from)) {
          if (k.equals("b")) {
            worklist.add(e.to);
          }
          if (k.equals("d")) {
            worklist.add(0, e.to);
          }
          this.find.add(next);
          path.put(e.to, next);
        }
        else if (!this.find.contains(e.from) && next.equals(e.to)) {
          if (k.equals("b")) {
            worklist.add(e.from);
          }
          if (k.equals("d")) {
            worklist.add(0, e.from);
          }
          this.find.add(next);
          path.put(e.from, next);
        }
      }
    } 
  }

  // Effect: reconstructions the path from the end to the beginning
  void reconstruct(HashMap<Vertex, Vertex> path, Vertex next) {
    this.finalPath.add(this.board.get(this.board.size() - 1).get(this.board.get(0).size() - 1));
    Vertex start = this.board.get(0).get(0);
    while (start != next) {
      this.finalPath.add(path.get(next));
      next = path.get(next);
    }
  }

  // compare the list of vertex is seen and the list of vertex in the solution
  void score(ArrayList<Vertex> find, ArrayList<Vertex> finalPath) {
    this.wrongMove = find.size() - finalPath.size();
  }

  // draw a vertex of the find and then the path
  public void onTick() {
    if (this.count > -1) {
      this.count += 1;
    }
    if (this.find.size() > 0) {
      if (this.count < this.find.size()) {
        Vertex s = this.find.get(this.count);
        s.isFindVertex = true;
      }
    }
    if (this.finalPath.size() > 0 && this.count > this.find.size()) {
      if (this.count - this.find.size() < this.finalPath.size()) {
        Vertex p = this.finalPath.get(this.count - this.find.size());
        p.isPathVertex = true;
      }
    } if (numPress >= 1) {
      this.time = this.time + tickRate;
    }
  }
}

// examples and tests of Maze world
class ExamplesMazeGame {
  MazeWorld world;

  ArrayList<ArrayList<Vertex>> board1;
  ArrayList<ArrayList<Vertex>> board2;
  ArrayList<ArrayList<Vertex>> board3;

  ArrayList<Vertex> list1;
  ArrayList<Vertex> list2;
  ArrayList<Vertex> list3;
  ArrayList<Vertex> list4;

  ArrayList<Vertex> rowlist1;
  ArrayList<Vertex> rowlist2;

  ArrayList<Edge> edge1;
  ArrayList<Edge> edge2;
  ArrayList<Edge> sortededges;
  ArrayList<Edge> edgesintree;

  Vertex a;
  Vertex b;
  Vertex c;
  Vertex d;
  Vertex e;
  Vertex f;

  Vertex a1;
  Vertex a2;
  Vertex a3;
  Vertex a4;
  Vertex a5;
  Vertex a6;

  Edge aToE;
  Edge aToB;
  Edge bToE;
  Edge bToC;
  Edge bToF;
  Edge cToD;
  Edge eToC;
  Edge fToD;

  Vertex g;
  Vertex h;
  Vertex i;
  Vertex j;
  Vertex k;
  Vertex l;

  Vertex g1;
  Vertex g2;
  Vertex g3;
  Vertex g4;
  Vertex g5;
  Vertex g6;

  Edge gToH;
  Edge gToJ;
  Edge hToI;
  Edge hToK;
  Edge jToI;
  Edge jToK;
  Edge kToI;
  Edge iToL;

  HashMap<Vertex, Vertex> representatives;
  HashMap<Vertex, Vertex> connectedRepresentatives;
  HashMap<Vertex, Vertex> path;
  ArrayList<Vertex> reconstructedPath;
  ArrayList<Vertex> findPath;
  ArrayList<Vertex> finalPath;

  Player player;

  // initializes the data
  void initMaze() {
    this.world = new MazeWorld();

    // examples of vertices for board1
    this.a = new Vertex(new Posn(0, 0), new ArrayList<Edge>());
    this.b = new Vertex(new Posn(0, 1), new ArrayList<Edge>());
    this.c = new Vertex(new Posn(0, 2), new ArrayList<Edge>());
    this.d = new Vertex(new Posn(1, 0), new ArrayList<Edge>());
    this.e = new Vertex(new Posn(1, 1), new ArrayList<Edge>());
    this.f = new Vertex(new Posn(1, 2), new ArrayList<Edge>());

    // examples of vertices in row for board1
    this.a1 = new Vertex(new Posn(0, 0), new ArrayList<Edge>());
    this.a2 = new Vertex(new Posn(0, 1), new ArrayList<Edge>());
    this.a3 = new Vertex(new Posn(0, 2), new ArrayList<Edge>());
    this.a4 = new Vertex(new Posn(1, 0), new ArrayList<Edge>());
    this.a5 = new Vertex(new Posn(1, 1), new ArrayList<Edge>());
    this.a6 = new Vertex(new Posn(1, 2), new ArrayList<Edge>());

    // example of edges for board1
    this.aToB = new Edge(this.a, this.b, 30);
    this.aToE = new Edge(this.a, this.e, 50);
    this.bToE = new Edge(this.b, this.e, 35);
    this.bToC = new Edge(this.b, this.c, 40);
    this.bToF = new Edge(this.b, this.f, 50);
    this.cToD = new Edge(this.c, this.d, 25);
    this.fToD = new Edge(this.f, this.d, 50);
    this.eToC = new Edge(this.e, this.c, 15);

    // examples of vertices for board2
    this.g = new Vertex(new Posn(0, 0), new ArrayList<Edge>());
    this.h = new Vertex(new Posn(0, 1), new ArrayList<Edge>());
    this.i = new Vertex(new Posn(0, 2), new ArrayList<Edge>());
    this.j = new Vertex(new Posn(1, 0), new ArrayList<Edge>());
    this.k = new Vertex(new Posn(1, 1), new ArrayList<Edge>());
    this.l = new Vertex(new Posn(1, 2), new ArrayList<Edge>());

    // examples of vertices in row for board2
    this.g1 = new Vertex(new Posn(0, 0), new ArrayList<Edge>());
    this.g2 = new Vertex(new Posn(0, 1), new ArrayList<Edge>());
    this.g3 = new Vertex(new Posn(0, 2), new ArrayList<Edge>());
    this.g4 = new Vertex(new Posn(1, 0), new ArrayList<Edge>());
    this.g5 = new Vertex(new Posn(1, 1), new ArrayList<Edge>());
    this.g6 = new Vertex(new Posn(1, 2), new ArrayList<Edge>());

    // example of edges for board2
    this.gToH = new Edge(this.g, this.h, 1);
    this.gToJ = new Edge(this.g, this.j, 1);
    this.hToI = new Edge(this.h, this.i, 1);
    this.hToK = new Edge(this.h, this.k, 1);
    this.iToL = new Edge(this.i, this.l, 1);
    this.jToK = new Edge(this.j, this.k, 1);
    this.kToI = new Edge(this.k, this.i, 1);

    this.list1 = new ArrayList<Vertex>(Arrays.asList(this.a, this.b, this.c));
    this.list2 = new ArrayList<Vertex>(Arrays.asList(this.d, this.e, this.f));
    this.board1 = new ArrayList<ArrayList<Vertex>>(Arrays.asList(this.list1, this.list2));
    this.edge1 = new ArrayList<Edge>(Arrays.asList(this.aToB, this.bToC, this.bToF, this.eToC,
        this.aToE, this.bToE, this.cToD, this.fToD));

    this.list3 = new ArrayList<Vertex>(Arrays.asList(this.g, this.h, this.i));
    this.list4 = new ArrayList<Vertex>(Arrays.asList(this.j, this.k, this.l));
    this.board2 = new ArrayList<ArrayList<Vertex>>(Arrays.asList(this.list3, this.list4));
    this.edge2 = new ArrayList<Edge>(
        Arrays.asList(this.gToH, this.gToJ, this.hToI, this.hToK, this.iToL, this.jToK, this.kToI));

    // add edges to vertexes
    this.a.outter.add(this.aToB);
    this.a.outter.add(this.aToE);
    this.b.outter.add(this.bToC);
    this.b.outter.add(this.bToE);
    this.b.outter.add(this.bToF);
    this.c.outter.add(this.cToD);
    this.f.outter.add(this.fToD);
    this.e.outter.add(this.eToC);

    // examples of representatives
    this.representatives = new HashMap<Vertex, Vertex>();
    this.representatives.put(this.a, this.a);
    this.representatives.put(this.b, this.b);
    this.representatives.put(this.c, this.c);
    this.representatives.put(this.d, this.d);
    this.representatives.put(this.e, this.f);
    this.representatives.put(this.f, this.f);

    // examples of two Representatives are connected
    this.connectedRepresentatives = new HashMap<Vertex, Vertex>();
    this.connectedRepresentatives.put(this.a, this.e);
    this.connectedRepresentatives.put(this.b, this.a);
    this.connectedRepresentatives.put(this.c, this.e);
    this.connectedRepresentatives.put(this.d, this.e);
    this.connectedRepresentatives.put(this.e, this.e);
    this.connectedRepresentatives.put(this.f, this.d);


    this.rowlist1 = new ArrayList<Vertex>(Arrays.asList(this.a1, this.a2, this.a3));
    this.rowlist2 = new ArrayList<Vertex>(Arrays.asList(this.a4, this.a5, this.a6));
    this.board3 = new ArrayList<ArrayList<Vertex>>(
        Arrays.asList(this.rowlist1, this.rowlist2));

    // example of hash map
    this.path = new HashMap<Vertex, Vertex>();
    this.path.put(this.a2, this.a1);
    this.path.put(this.a3, this.a2);
    this.path.put(this.a4, this.a3);
    this.path.put(this.a5, this.a4);
    this.path.put(this.a6, this.a4);

    this.sortededges = new ArrayList<Edge>(Arrays.asList(this.eToC, this.cToD, this.aToB, this.bToE,
        this.bToC, this.fToD, this.aToE, this.bToF));

    this.edgesintree = new ArrayList<Edge>(
        Arrays.asList(this.eToC, this.cToD, this.aToB, this.bToE, this.fToD));

    this.reconstructedPath = new ArrayList<Vertex>(
        Arrays.asList( this.a6, this.a3, this.a2, this.a1));

    this.findPath = new ArrayList<Vertex>(
        Arrays.asList(this.a6, this.a5, this.a4, this.a3, this.a2, this.a1));

    this.finalPath = new ArrayList<Vertex>(
        Arrays.asList(this.a6, this.a4, this.a3, this.a2, this.a1));

    this.player = new Player(0, 0);
  }

  // tests make board method
  void testMakeBoard(Tester t) {
    this.initMaze();
    for (int x = 0; x < this.world.makeBoard().size(); x++) {
      for (int y = 0; y < this.world.makeBoard().get(x).size(); y++) {
        Vertex v = this.world.makeBoard().get(x).get(y);
        ArrayList<Edge> e = new ArrayList<Edge>();
        t.checkExpect(v, new Vertex(new Posn(x, y), e));
      }
    }
  }

  // tests make edges method
  void testMakeEdges(Tester t) {
    this.initMaze();
    t.checkExpect(this.world.makeEdges(), this.edge2);
  }

  // tests sort edges method
  void testSortEdges(Tester t) {
    this.initMaze();
    t.checkExpect(this.world.sortEdges(this.edge1), this.sortededges);
  }

  // tests minSpaningTree method
  void testMinSpanningTree(Tester t) {
    this.initMaze();
    t.checkExpect(this.world.edgesInTree.size(), MazeWorld.HEIGHT * MazeWorld.WIDTH - 1);
    for (int i = 0; i < this.world.edgesInTree.size() - 1; i++) {
      Edge edge1 = this.world.edgesInTree.get(i);
      Edge edge2 = this.world.edgesInTree.get(i + 1);
      t.checkExpect(edge1.weight < edge2.weight, true);
    }
  }

  // tests find method
  void testFind(Tester t) {
    this.initMaze();
    t.checkExpect(this.world.find(this.connectedRepresentatives, this.c), this.e);
    t.checkExpect(this.world.find(this.connectedRepresentatives, this.e), this.e);
    t.checkExpect(this.world.find(this.connectedRepresentatives, this.f), this.e);
  }

  // tests union method
  void testUnion(Tester t) {
    this.initMaze();
    this.world.union(this.representatives, this.a, this.b);
    t.checkExpect(this.representatives.get(this.a), this.b);
    this.initMaze();
    this.world.union(this.representatives, this.c, this.d);
    t.checkExpect(this.representatives.get(this.c), this.d);
    this.initMaze();
    this.world.union(this.representatives, this.e, this.b);
    t.checkExpect(this.representatives.get(this.e), this.b);
  }

  // tests assignEdges method
  void testAssignEdges(Tester t) {
    this.initMaze();
    this.world.assignEdges();
    for (Edge e : this.world.edgesInTree) {
      t.checkExpect(e.from.outter.contains(e), true);
      t.checkExpect(e.to.outter.contains(e), true);
    }
  }

  // tests make scene method
  void testMakeScene(Tester t) {
    this.initMaze();

    MazeWorld maze = new MazeWorld();
    WorldScene scene = new WorldScene(3, 3);
    for (ArrayList<Vertex> vertex : board1) {
      for (Vertex v : vertex) {
        scene.placeImageXY(v.drawCell(), v.position.x * 10 + 5, v.position.y * 10 + 5);
      }
      for (Edge e : edge1) {
        scene.placeImageXY(e.drawEdge(), (e.to.position.x + e.from.position.x) * 5 + 5,
            (e.to.position.y + e.from.position.y) * 5 + 5);

        scene.placeImageXY(new RectangleImage(9, 9, OutlineMode.SOLID, Color.green),
            MazeWorld.WIDTH * 10 - 5, MazeWorld.HEIGHT * 10 - 5);

        // draw the find vertex
        for (Vertex v : this.findPath) {
          scene.placeImageXY(v.drawFindVertex(), v.position.x * 10 + 5, v.position.y * 10 + 5);
        }
      }

      // draw the player vertex
      scene.placeImageXY(this.player.drawPlayer(), player.x * 10 + 5, player.y * 10 + 5);

      // draw the timer on the board
      scene.placeImageXY(new TextImage("Time: " + "0" + "s", 
          MazeWorld.WIDTH * MazeWorld.HEIGHT / 25, Color.black), 
          MazeWorld.WIDTH * MazeWorld.CELL_SIZE - 15, 10);
      //MazeWorld.WIDTH * MazeWorld.CELL_SIZE / 2, (MazeWorld.CELL_SIZE + 2) * MazeWorld.HEIGHT);

      // draw the player moves on the board
      scene.placeImageXY(new TextImage("Moves: " + "0", 
          MazeWorld.WIDTH * MazeWorld.HEIGHT / 25, Color.black), 
          MazeWorld.WIDTH * MazeWorld.CELL_SIZE - 15, 25);

      // draw the title of the maze game
      scene.placeImageXY(new TextImage("Maze Game", 
          MazeWorld.WIDTH * MazeWorld.HEIGHT / 15, Color.black), 
          MazeWorld.WIDTH * MazeWorld.CELL_SIZE / 2, (MazeWorld.CELL_SIZE + 2) * MazeWorld.HEIGHT);

    }
    t.checkExpect(maze.makeScene(), scene);
  }

  // tests isPath method
  void testisPath(Tester t) {
    this.initMaze();
    t.checkExpect(this.world.isPath(0, 0, 0, 0), true);
    t.checkExpect(this.world.isPath(1, 0, 0, 1), false);
    t.checkExpect(this.world.isPath(1, 1, 1, 1), true);
  }

  // tests onKeyEvent method
  void testOnKeyEvent(Tester t) {
    this.initMaze();
    this.world.onKeyEvent("up");
    t.checkOneOf(this.player.y, -1, 0);
    this.world.onKeyEvent("down");
    t.checkOneOf(this.player.y, 0, 1);
    this.world.onKeyEvent("left");
    t.checkOneOf(this.player.x, -1, 0);
    this.world.onKeyEvent("right");
    t.checkOneOf(this.player.x, 0, 1);
    this.world.onKeyEvent("b");
    t.checkExpect(this.world.find.size() > 0, true);
    t.checkExpect(this.world.finalPath.size() > 0, true);
    this.world.onKeyEvent("r");
    t.checkExpect(this.world.find.size() == 0, true);
    t.checkExpect(this.world.finalPath.size() == 0, true);
    t.checkExpect(this.world.count == -1, true);
    this.world.onKeyEvent("d");
    t.checkExpect(this.world.find.size() > 0, true);
    t.checkExpect(this.world.finalPath.size() > 0, true);
  }

  // tests find path method 
  void testFindPath(Tester t) {
    this.initMaze();
    this.world.findPath("d");
    t.checkExpect(this.world.find.size() > 0, true);
    t.checkExpect(this.world.find.size() < MazeWorld.HEIGHT * MazeWorld.WIDTH, true);
    t.checkExpect(this.world.finalPath.get(0),
        this.world.board.get(this.world.board.size() - 1).get(this.world.board.get(0).size() - 1));
    t.checkExpect(this.world.finalPath.get(this.world.finalPath.size() - 1), 
        this.world.board.get(0).get(0));
    this.world.findPath("b");
    t.checkExpect(this.world.find.size() > 0, true);
    t.checkExpect(this.world.find.size() < MazeWorld.HEIGHT * MazeWorld.WIDTH, true);
    t.checkExpect(this.world.finalPath.get(0),
        this.world.board.get(this.world.board.size() - 1).get(this.world.board.get(0).size() - 1));
    t.checkExpect(this.world.finalPath.get(this.world.finalPath.size() - 1), 
        this.world.board.get(0).get(0));
  }

  // tests score method
  void testScore(Tester t) {
    this.initMaze();
    this.world.score(findPath, finalPath);
    t.checkExpect(this.world.wrongMove, 1);
  }

  // tests reconstruct method
  void testReconstruct(Tester t) {
    this.initMaze();
    this.world.board = this.board3;
    this.world.reconstruct(this.path, this.a4);
    t.checkExpect(this.world.finalPath, this.reconstructedPath);
  }

  // tests equals method
  boolean testEquals(Tester t) {
    this.initMaze();
    return t.checkExpect(this.a.equals(this.a), true) 
        && t.checkExpect(this.b.equals(this.c), false)
        && t.checkExpect(this.aToB.equals(this.aToB), true)
        && t.checkExpect(this.bToC.equals(this.eToC), false);
  }

  // tests hashCode method
  void testHashCode(Tester t) {
    this.initMaze();
    Object object = new Vertex(new Posn(0, 0), new ArrayList<Edge>());
    t.checkExpect(this.a.equals(object), true);
    t.checkExpect(this.a.hashCode() == object.hashCode(), true);
    t.checkExpect(this.b.equals(object), false);
  }

  // tests drawCell method
  boolean testDrawCell(Tester t) {
    this.initMaze();
    return t.checkExpect(this.d.drawCell(),
        new RectangleImage(9, 9, OutlineMode.SOLID, Color.LIGHT_GRAY))
        && t.checkExpect(this.e.drawCell(),
            new RectangleImage(9, 9, OutlineMode.SOLID, Color.LIGHT_GRAY));
  }

  // tests drawFindVertex
  boolean testDrawFindVertex(Tester t) {
    this.initMaze();
    this.a.isFindVertex = true;
    return t.checkExpect(this.a.drawFindVertex(),
        new RectangleImage(9, 9, OutlineMode.SOLID, Color.yellow))
        && t.checkExpect(this.e.drawFindVertex(), new EmptyImage());
  }

  // tests drawPathVertex
  boolean testDrawPathVertex(Tester t) {
    this.initMaze();
    this.b.isPathVertex = true;
    return t.checkExpect(this.b.drawPathVertex(),
        new RectangleImage(9, 9, OutlineMode.SOLID, Color.cyan))
        && t.checkExpect(this.e.drawPathVertex(), new EmptyImage());
  }

  // tests drawPlayerVertex
  boolean testDrawPlayerVertex(Tester t) {
    this.initMaze();
    return t.checkExpect(this.c.drawPlayerVertex(),
        new RectangleImage(9, 9, OutlineMode.SOLID, Color.pink));
  }

  // tests compareTo method
  boolean testCompareTo(Tester t) {
    this.initMaze();
    return t.checkExpect(edge1.get(0).compareTo(edge1.get(1)), -1)
        && t.checkExpect(edge1.get(2).compareTo(edge1.get(1)), 1)
        && t.checkExpect(edge1.get(0).compareTo(edge1.get(0)), 0);
  }

  // tests drawEdge method
  boolean testDrawEdge(Tester t) {
    this.initMaze();
    return t.checkExpect(this.eToC.drawEdge(),
        new RectangleImage(9, 9, OutlineMode.SOLID, Color.LIGHT_GRAY));
  }

  // tests drawPlayer method
  boolean testDrawPlayer(Tester t) {
    this.initMaze();
    return t.checkExpect(this.player.drawPlayer(),
        new RectangleImage(9, 9, OutlineMode.SOLID, Color.yellow));
  }

  // play the game
  void testGame(Tester t) {
    MazeWorld game = new MazeWorld();
    game.bigBang(MazeWorld.WIDTH * (MazeWorld.CELL_SIZE + 1), 
        (MazeWorld.HEIGHT + 3) * (MazeWorld.CELL_SIZE + 1), 0.1);
  }
}