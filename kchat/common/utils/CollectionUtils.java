package kchat.common.utils;

import java.util.Iterator;

public final class CollectionUtils {
	public static final class Tuple<L, R>{
		public final L _1;
		public final R _2;
		public Tuple(L left, R right) {
			_1 = left;
			_2 = right;
		}
		
		public String toString() {
			return "(" + _1 + "," + _2 + ")";
		}
	}
	//merge tuple
	public static final <L,R> Tuple<L,R> Tuple(L left, R right){
		return new Tuple<>(left, right);
	}
	
	// Compress iterator 
	public static <L,R> Iterator<Tuple<L,R>> zipIterator(final Iterator<L> left, final Iterator<R> right){
		return new Iterator<Tuple<L,R>>(){
			
			public boolean hasNext() {
				return left.hasNext() && right.hasNext();
			}
			
			public Tuple<L,R> next() {
				return Tuple(left.next(), right.next());
			}
		};
	}
	
	public static <L,R> Iterable<Tuple<L,R>> zip(final Iterable<L> left, final Iterable<R> right){
		return new Iterable<Tuple<L,R>>(){
			public Iterator<Tuple<L,R>> iterator(){
				return zipIterator(left.iterator(), right.iterator());
			}
		};
	}
}
