package vendredi.soir.nofy.character;

import java.util.ArrayList;
import java.util.List;
import vendredi.soir.nofy.data.LimbPose;
import vendredi.soir.nofy.data.PoseDefinition;

/**
 * A pose with every field filled in and every IK target solved: what a clip interpolates and what
 * the applier turns into bone rotations. Angles are in the prototype's conventions (see
 * docs/rigged-characters.md).
 */
public class ResolvedPose {
  public static final int ARM_L = 0;
  public static final int ARM_R = 1;
  public static final int LEG_L = 2;
  public static final int LEG_R = 3;
  public static final String[] LIMB_NAMES = {"armL", "armR", "legL", "legR"};

  public String facing;
  public String look;
  public String expr;
  public String seat;
  public float spine;
  public float neck;
  public float rootX;
  public float rootY;
  public float rootRot;

  /** Problems found while resolving, e.g. an IK target out of reach. */
  public final List<String> warnings = new ArrayList<>();

  public final Limb[] limbs = {new Limb(), new Limb(), new Limb(), new Limb()};

  public static class Limb {
    public float a;
    public float b;
    public float s1 = 1;
    public float s2 = 1;
    public float foot;
    public float wrist;
    public int bend;
    public String hand;
    public String layer;

    Limb copy() {
      Limb c = new Limb();
      c.a = a;
      c.b = b;
      c.s1 = s1;
      c.s2 = s2;
      c.foot = foot;
      c.wrist = wrist;
      c.bend = bend;
      c.hand = hand;
      c.layer = layer;
      return c;
    }
  }

  public ResolvedPose copy() {
    ResolvedPose c = new ResolvedPose();
    c.facing = facing;
    c.look = look;
    c.expr = expr;
    c.seat = seat;
    c.spine = spine;
    c.neck = neck;
    c.rootX = rootX;
    c.rootY = rootY;
    c.rootRot = rootRot;
    c.warnings.addAll(warnings);
    for (int i = 0; i < 4; i++) {
      c.limbs[i] = limbs[i].copy();
    }
    return c;
  }

  /**
   * Overlays whatever the definition sets, field by field, without solving IK: this is how a clip
   * key's "set" lands on an already-resolved pose.
   */
  public void overlay(PoseDefinition d) {
    if (d.getFacing() != null) facing = d.getFacing();
    if (d.getLook() != null) look = d.getLook();
    if (d.getExpr() != null) expr = d.getExpr();
    if (d.getSeat() != null) seat = d.getSeat();
    if (d.getSpine() != null) spine = d.getSpine();
    if (d.getNeck() != null) neck = d.getNeck();
    if (d.getRoot() != null) {
      if (d.getRoot().getX() != null) rootX = d.getRoot().getX();
      if (d.getRoot().getY() != null) rootY = d.getRoot().getY();
      if (d.getRoot().getRot() != null) rootRot = d.getRoot().getRot();
    }
    LimbPose[] defs = {d.getArmL(), d.getArmR(), d.getLegL(), d.getLegR()};
    for (int i = 0; i < 4; i++) {
      if (defs[i] == null) continue;
      Limb l = limbs[i];
      LimbPose s = defs[i];
      if (s.getA() != null) l.a = s.getA();
      if (s.getB() != null) l.b = s.getB();
      if (s.getS1() != null) l.s1 = s.getS1();
      if (s.getS2() != null) l.s2 = s.getS2();
      if (s.getFoot() != null) l.foot = s.getFoot();
      if (s.getWrist() != null) l.wrist = s.getWrist();
      if (s.getBend() != null) l.bend = s.getBend();
      if (s.getHand() != null) l.hand = s.getHand();
      if (s.getLayer() != null) l.layer = s.getLayer();
    }
  }

  /** Numeric fields blend linearly; discrete ones (bend, hand, layer, ...) switch at t >= 1. */
  public static ResolvedPose lerp(ResolvedPose a, ResolvedPose b, float t) {
    ResolvedPose o = new ResolvedPose();
    boolean end = t >= 1f;
    o.facing = end ? b.facing : a.facing;
    o.look = end ? b.look : a.look;
    o.expr = end ? b.expr : a.expr;
    o.seat = end ? b.seat : a.seat;
    o.spine = mix(a.spine, b.spine, t);
    o.neck = mix(a.neck, b.neck, t);
    o.rootX = mix(a.rootX, b.rootX, t);
    o.rootY = mix(a.rootY, b.rootY, t);
    o.rootRot = mix(a.rootRot, b.rootRot, t);
    for (int i = 0; i < 4; i++) {
      Limb x = a.limbs[i];
      Limb y = b.limbs[i];
      Limb l = o.limbs[i];
      l.a = mix(x.a, y.a, t);
      l.b = mix(x.b, y.b, t);
      l.s1 = mix(x.s1, y.s1, t);
      l.s2 = mix(x.s2, y.s2, t);
      l.foot = mix(x.foot, y.foot, t);
      l.wrist = mix(x.wrist, y.wrist, t);
      l.bend = end ? y.bend : x.bend;
      l.hand = end ? y.hand : x.hand;
      l.layer = end ? y.layer : x.layer;
    }
    return o;
  }

  /** Sets one control by track path, e.g. "armR.wrist" or "root.y". */
  public void set(String path, float v) {
    switch (path) {
      case "spine" -> spine = v;
      case "neck" -> neck = v;
      case "root.x" -> rootX = v;
      case "root.y" -> rootY = v;
      case "root.rot" -> rootRot = v;
      default -> {
        int dot = path.indexOf('.');
        Limb l = limbs[limbIndex(path.substring(0, dot))];
        switch (path.substring(dot + 1)) {
          case "a" -> l.a = v;
          case "b" -> l.b = v;
          case "s1" -> l.s1 = v;
          case "s2" -> l.s2 = v;
          case "foot" -> l.foot = v;
          case "wrist" -> l.wrist = v;
          default -> throw new IllegalArgumentException("Unknown control: " + path);
        }
      }
    }
  }

  public static int limbIndex(String name) {
    for (int i = 0; i < 4; i++) {
      if (LIMB_NAMES[i].equals(name)) return i;
    }
    throw new IllegalArgumentException("Unknown limb: " + name);
  }

  private static float mix(float a, float b, float t) {
    return a + (b - a) * t;
  }
}
