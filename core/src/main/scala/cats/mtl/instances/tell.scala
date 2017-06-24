package cats
package mtl
package instances

import cats.data.WriterT

trait TellInstances extends TellInstancesLowPriority1 {
  implicit final def tellInd[M[_], Inner[_], L](implicit
                                                   lift: ApplicativeLayer[M, Inner],
                                                   under: ApplicativeTell[Inner, L]
                                               ): ApplicativeTell[M, L] = {
    new ApplicativeTell[M, L] {
      val applicative: Applicative[M] = lift.outerInstance
      val monoid: Monoid[L] = under.monoid

      def tell(l: L): M[Unit] = lift.layer(under.tell(l))

      def writer[A](a: A, l: L): M[A] = lift.layer(under.writer(a, l))

      def tuple[A](ta: (L, A)): M[A] = lift.layer(under.tuple(ta))
    }
  }
}

private[instances] trait TellInstancesLowPriority1 {
  implicit final def tellWriter[M[_], L](implicit L: Monoid[L], M: Applicative[M]): ApplicativeTell[CurryT[WriterTCL[L]#l, M]#l, L] = {
    new ApplicativeTell[CurryT[WriterTCL[L]#l, M]#l, L] {
      val applicative = WriterT.catsDataApplicativeForWriterT(M, L)
      val monoid: Monoid[L] = L

      def tell(l: L): WriterT[M, L, Unit] = WriterT.tell(l)

      def writer[A](a: A, l: L): WriterT[M, L, A] = WriterT.put(a)(l)

      def tuple[A](ta: (L, A)): WriterT[M, L, A] = WriterT(M.pure(ta))
    }
  }

  implicit final def tellTuple[L](implicit L: Monoid[L]): ApplicativeTell[TupleC[L]#l, L] = {
    new ApplicativeTell[TupleC[L]#l, L] {
      val applicative = cats.instances.tuple.catsStdMonadForTuple2
      val monoid: Monoid[L] = L

      def tell(l: L): (L, Unit) = (l, ())

      def writer[A](a: A, l: L): (L, A) = (l, a)

      def tuple[A](ta: (L, A)): (L, A) = ta
    }
  }
}

object tell extends TellInstances