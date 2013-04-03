package org.embl.cca.utils.threading.MUSC;

public interface IMultiUserStaticClassListener<CLASS> {
	public class WrappedObject<CLASS> {
		protected CLASS object;

		public WrappedObject(CLASS object) {
			this.object = object;
		}

		public void setObject(CLASS object) {
			this.object = object;
		}

		public CLASS getObject() {
			return object;
		}
	}

	public void createObject(WrappedObject<CLASS> wrappedObject);
	public void disposeObject(final WrappedObject<CLASS> wrappedObject);
}
