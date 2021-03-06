package org.encog.ml.prg.extension;

import java.util.Random;

import org.encog.Encog;
import org.encog.mathutil.randomize.RangeRandomizer;
import org.encog.ml.prg.EncogProgram;
import org.encog.ml.prg.ExpressionError;
import org.encog.ml.prg.epl.EPLHolder;
import org.encog.ml.prg.epl.EPLUtil;
import org.encog.ml.prg.epl.OpCodeHeader;
import org.encog.ml.prg.expvalue.ExpressionValue;
import org.encog.ml.prg.train.GeneticTrainingParams;

public class StandardExtensions {

	public static final short OPCODE_CONST_FLOAT = 0;
	public static final short OPCODE_VAR = 1;
	public static final short OPCODE_NEG = 2;
	public static final short OPCODE_ADD = 3;
	public static final short OPCODE_SUB = 4;
	public static final short OPCODE_MUL = 5;
	public static final short OPCODE_DIV = 6;
	public static final short OPCODE_POW = 7;
	public static final short OPCODE_AND = 8;
	public static final short OPCODE_OR = 9;
	public static final short OPCODE_EQUAL = 10;
	public static final short OPCODE_GT = 11;
	public static final short OPCODE_LT = 12;
	public static final short OPCODE_ABS = 13;
	public static final short OPCODE_ACOS = 14;
	public static final short OPCODE_ASIN = 15;
	public static final short OPCODE_ATAN = 16;
	public static final short OPCODE_ATAN2 = 17;
	public static final short OPCODE_CEIL = 18;
	public static final short OPCODE_COS = 19;
	public static final short OPCODE_COSH = 20;
	public static final short OPCODE_EXP = 21;
	public static final short OPCODE_FLOOR = 22;
	public static final short OPCODE_LOG = 23;
	public static final short OPCODE_LOG10 = 24;
	public static final short OPCODE_MAX = 25;
	public static final short OPCODE_MIN = 26;
	public static final short OPCODE_POWFN = 27;
	public static final short OPCODE_RAND = 28;
	public static final short OPCODE_ROUND = 29;
	public static final short OPCODE_SIN = 30;
	public static final short OPCODE_SINH = 31;
	public static final short OPCODE_SQRT = 32;
	public static final short OPCODE_TAN = 33;
	public static final short OPCODE_TANH = 34;
	public static final short OPCODE_TODEG = 35;
	public static final short OPCODE_TORAD = 36;
	public static final short OPCODE_LENGTH = 37;
	public static final short OPCODE_FORMAT = 38;
	public static final short OPCODE_LEFT = 39;
	public static final short OPCODE_RIGHT = 40;
	public static final short OPCODE_CINT = 41;
	public static final short OPCODE_CFLOAT = 42;
	public static final short OPCODE_CSTR = 43;
	public static final short OPCODE_CBOOL = 44;
	public static final short OPCODE_IFF = 45;
	public static final short OPCODE_CLAMP = 46;
	public static final short OPCODE_GTE = 47;
	public static final short OPCODE_LTE = 48;
	public static final short OPCODE_CONST_BOOL = 49;
	public static final short OPCODE_CONST_INT = 50;
	public static final short OPCODE_CONST_STRING = 51;

	/**
	 * Standard unary minus operator.
	 */
	public static ProgramExtensionTemplate EXTENSION_VAR_SUPPORT = new BasicTemplate(
			OPCODE_VAR) {

		@Override
		public void evaluate(final EncogProgram prg) {
			final int idx = prg.getHeader().getParam2();
			final ExpressionValue result = prg.getVariables().getVariable(idx);
			if (result == null) {
				throw new ExpressionError("Variable has no value: "
						+ prg.getVariables().getVariableName(idx));
			}
			prg.getStack().push(result);
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}

		@Override
		public void randomize(final Random r, final EncogProgram program,
				final double degree) {
			final int range = program.getVariables().size();
			short index = 0;
			if (range > 0) {
				index = (short) r.nextInt(range);
			}
			program.writeNode(getOpcode(), 0, index);
		}
	};

	/**
	 * Floating point const.
	 */
	public static ProgramExtensionTemplate EXTENSION_CONST_FLOAT = new BasicTemplate(
			OPCODE_CONST_FLOAT) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(prg.readDouble());
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 2;
		}

		@Override
		public void randomize(final Random r, final EncogProgram program,
				final double degree) {
			final GeneticTrainingParams params = program.getContext()
					.getParams();
			program.writeNode(getOpcode());
			program.writeDouble(RangeRandomizer.randomize(r,
					params.getConstMin(), params.getConstMax()));
		}
	};

	/**
	 * Floating point const.
	 */
	public static ProgramExtensionTemplate EXTENSION_CONST_BOOL = new BasicTemplate(
			OPCODE_CONST_BOOL) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack()
					.push(prg.getHeader().getParam2() == 0 ? false : true);
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}

		@Override
		public void randomize(final Random r, final EncogProgram program,
				final double degree) {
			program.writeNode(getOpcode(), r.nextBoolean() ? 1 : 0, (short) 0);
		}
	};

	/**
	 * Int const.
	 */
	public static ProgramExtensionTemplate EXTENSION_CONST_INT = new BasicTemplate(
			OPCODE_CONST_INT) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(prg.getHeader().getParam1());
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}

		@Override
		public void randomize(final Random r, final EncogProgram program,
				final double degree) {
			final GeneticTrainingParams params = program.getContext()
					.getParams();
			final int value = (int) RangeRandomizer.randomize(r,
					params.getConstMin(), params.getConstMax());
			program.writeNode(getOpcode(), value, (short) 0);
		}
	};

	/**
	 * String const.
	 */
	public static ProgramExtensionTemplate EXTENSION_CONST_STRING = new BasicTemplate(
			OPCODE_CONST_STRING) {
		@Override
		public void evaluate(final EncogProgram prg) {
			final String str = prg.readString(prg.getHeader().getParam2());
			prg.getStack().push(str);
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			int i = EPLUtil.roundToFrame(header.getParam2());
			i /= EPLHolder.FRAME_SIZE;
			return 1 + i;
		}

		@Override
		public void randomize(final Random r, final EncogProgram program,
				final double degree) {
			final GeneticTrainingParams params = program.getContext()
					.getParams();
			program.writeNode(getOpcode(), 0, (short) 0);
		}
	};

	/**
	 * Standard unary minus operator.
	 */
	public static ProgramExtensionTemplate EXTENSION_NEG = new BasicTemplate(
			OPCODE_NEG) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(-prg.getStack().pop().toFloatValue());
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard binary add operator.
	 */
	public static ProgramExtensionTemplate EXTENSION_ADD = new BasicTemplate(
			OPCODE_ADD) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().operationAdd();
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard binary subtract operator.
	 */
	public static ProgramExtensionTemplate EXTENSION_SUB = new BasicTemplate(
			OPCODE_SUB) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().operationSub();
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard binary multiply operator.
	 */
	public static ProgramExtensionTemplate EXTENSION_MUL = new BasicTemplate(
			OPCODE_MUL) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().operationMul();
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard binary divide operator.
	 */
	public static ProgramExtensionTemplate EXTENSION_DIV = new BasicTemplate(
			OPCODE_DIV) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().operationDiv();
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard binary power operator.
	 */
	public static ProgramExtensionTemplate EXTENSION_POWER = new BasicTemplate(
			OPCODE_POW) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().operationPow();
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard boolean binary and operator.
	 */
	public static ProgramExtensionTemplate EXTENSION_AND = new BasicTemplate(
			OPCODE_AND) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(
					prg.getStack().pop().toBooleanValue()
							&& prg.getStack().pop().toBooleanValue());
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard boolean binary or operator.
	 */
	public static ProgramExtensionTemplate EXTENSION_OR = new BasicTemplate(
			OPCODE_OR) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(
					prg.getStack().pop().toBooleanValue()
							|| prg.getStack().pop().toBooleanValue());
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard binary equal operator.
	 */
	public static ProgramExtensionTemplate EXTENSION_EQUAL = new BasicTemplate(
			OPCODE_EQUAL) {
		@Override
		public void evaluate(final EncogProgram prg) {
			final double diff = Math.abs(prg.getStack().pop().toFloatValue()
					- prg.getStack().pop().toFloatValue());
			prg.getStack().push(diff < Encog.DEFAULT_DOUBLE_EQUAL);
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard boolean greater than operator.
	 */
	public static ProgramExtensionTemplate EXTENSION_GT = new BasicTemplate(
			OPCODE_GT) {
		@Override
		public void evaluate(final EncogProgram prg) {
			final double b = prg.getStack().pop().toFloatValue();
			final double a = prg.getStack().pop().toFloatValue();
			prg.getStack().push(a > b);
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard boolean less than operator.
	 */
	public static ProgramExtensionTemplate EXTENSION_LT = new BasicTemplate(
			OPCODE_LT) {
		@Override
		public void evaluate(final EncogProgram prg) {
			final double b = prg.getStack().pop().toFloatValue();
			final double a = prg.getStack().pop().toFloatValue();
			prg.getStack().push(a < b);
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard boolean greater than or equal operator.
	 */
	public static ProgramExtensionTemplate EXTENSION_GTE = new BasicTemplate(
			OPCODE_GTE) {
		@Override
		public void evaluate(final EncogProgram prg) {
			final double b = prg.getStack().pop().toFloatValue();
			final double a = prg.getStack().pop().toFloatValue();
			prg.getStack().push(a >= b);
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard boolean less than or equal operator.
	 */
	public static ProgramExtensionTemplate EXTENSION_LTE = new BasicTemplate(
			OPCODE_LTE) {
		@Override
		public void evaluate(final EncogProgram prg) {
			final double b = prg.getStack().pop().toFloatValue();
			final double a = prg.getStack().pop().toFloatValue();
			prg.getStack().push(a <= b);
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard numeric absolute value function.
	 */
	public static ProgramExtensionTemplate EXTENSION_ABS = new BasicTemplate(
			OPCODE_ABS) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(Math.abs(prg.getStack().pop().toFloatValue()));
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard numeric acos function.
	 */
	public static ProgramExtensionTemplate EXTENSION_ACOS = new BasicTemplate(
			OPCODE_ACOS) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(Math.acos(prg.getStack().pop().toFloatValue()));
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard numeric asin function.
	 */
	public static ProgramExtensionTemplate EXTENSION_ASIN = new BasicTemplate(
			OPCODE_ASIN) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(Math.asin(prg.getStack().pop().toFloatValue()));
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard numeric atan function.
	 */
	public static ProgramExtensionTemplate EXTENSION_ATAN = new BasicTemplate(
			OPCODE_ATAN) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(Math.atan(prg.getStack().pop().toFloatValue()));
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard numeric atan2 function.
	 */
	public static ProgramExtensionTemplate EXTENSION_ATAN2 = new BasicTemplate(
			OPCODE_ATAN2) {
		@Override
		public void evaluate(final EncogProgram prg) {
			final double a = prg.getStack().pop().toFloatValue();
			final double b = prg.getStack().pop().toFloatValue();
			prg.getStack().push(Math.atan2(a, b));
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard numeric ceil function.
	 */
	public static ProgramExtensionTemplate EXTENSION_CEIL = new BasicTemplate(
			OPCODE_CEIL) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(Math.ceil(prg.getStack().pop().toFloatValue()));
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard numeric cos function.
	 */
	public static ProgramExtensionTemplate EXTENSION_COS = new BasicTemplate(
			OPCODE_COS) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(Math.cos(prg.getStack().pop().toFloatValue()));
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard numeric cosh function.
	 */
	public static ProgramExtensionTemplate EXTENSION_COSH = new BasicTemplate(
			OPCODE_COSH) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(Math.cosh(prg.getStack().pop().toFloatValue()));
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard numeric exp function.
	 */
	public static ProgramExtensionTemplate EXTENSION_EXP = new BasicTemplate(
			OPCODE_EXP) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(Math.exp(prg.getStack().pop().toFloatValue()));
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard numeric floor function.
	 */
	public static ProgramExtensionTemplate EXTENSION_FLOOR = new BasicTemplate(
			OPCODE_FLOOR) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack()
					.push(Math.floor(prg.getStack().pop().toFloatValue()));
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard numeric log function.
	 */
	public static ProgramExtensionTemplate EXTENSION_LOG = new BasicTemplate(
			OPCODE_LOG) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(Math.log(prg.getStack().pop().toFloatValue()));
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard numeric log10 function.
	 */
	public static ProgramExtensionTemplate EXTENSION_LOG10 = new BasicTemplate(
			OPCODE_LOG10) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack()
					.push(Math.log10(prg.getStack().pop().toFloatValue()));
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard numeric max function.
	 */
	public static ProgramExtensionTemplate EXTENSION_MAX = new BasicTemplate(
			OPCODE_MAX) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(
					Math.max(prg.getStack().pop().toFloatValue(), prg
							.getStack().pop().toFloatValue()));
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard numeric min function.
	 */
	public static ProgramExtensionTemplate EXTENSION_MIN = new BasicTemplate(
			OPCODE_MIN) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(
					Math.min(prg.getStack().pop().toFloatValue(), prg
							.getStack().pop().toFloatValue()));
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard numeric pow function.
	 */
	public static ProgramExtensionTemplate EXTENSION_POWFN = new BasicTemplate(
			OPCODE_POWFN) {
		@Override
		public void evaluate(final EncogProgram prg) {
			final double b = prg.getStack().pop().toFloatValue();
			final double a = prg.getStack().pop().toFloatValue();
			prg.getStack().push(Math.pow(a, b));
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard numeric random function.
	 */
	public static ProgramExtensionTemplate EXTENSION_RANDOM = new BasicTemplate(
			OPCODE_RAND) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(Math.random());
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard numeric round function.
	 */
	public static ProgramExtensionTemplate EXTENSION_ROUND = new BasicTemplate(
			OPCODE_ROUND) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack()
					.push(Math.round(prg.getStack().pop().toFloatValue()));
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard numeric sin function.
	 */
	public static ProgramExtensionTemplate EXTENSION_SIN = new BasicTemplate(
			OPCODE_SIN) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack()
					.push(Math.round(prg.getStack().pop().toFloatValue()));
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard numeric sinh function.
	 */
	public static ProgramExtensionTemplate EXTENSION_SINH = new BasicTemplate(
			OPCODE_SINH) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(Math.sinh(prg.getStack().pop().toFloatValue()));
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard numeric sqrt function.
	 */
	public static ProgramExtensionTemplate EXTENSION_SQRT = new BasicTemplate(
			OPCODE_SQRT) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(Math.sqrt(prg.getStack().pop().toFloatValue()));
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard numeric tan function.
	 */
	public static ProgramExtensionTemplate EXTENSION_TAN = new BasicTemplate(
			OPCODE_TAN) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(Math.tan(prg.getStack().pop().toFloatValue()));
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard numeric tanh function.
	 */
	public static ProgramExtensionTemplate EXTENSION_TANH = new BasicTemplate(
			OPCODE_TANH) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(Math.tanh(prg.getStack().pop().toFloatValue()));
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard numeric todegress function.
	 */
	public static ProgramExtensionTemplate EXTENSION_TODEG = new BasicTemplate(
			OPCODE_TODEG) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(
					Math.toDegrees(prg.getStack().pop().toFloatValue()));
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard numeric toradians function.
	 */
	public static ProgramExtensionTemplate EXTENSION_TORAD = new BasicTemplate(
			OPCODE_TORAD) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(
					Math.toRadians(prg.getStack().pop().toFloatValue()));
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard string length function.
	 */
	public static ProgramExtensionTemplate EXTENSION_LENGTH = new BasicTemplate(
			OPCODE_LENGTH) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(prg.getStack().pop().toStringValue().length());
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard string format function.
	 */
	public static ProgramExtensionTemplate EXTENSION_FORMAT = new BasicTemplate(
			OPCODE_FORMAT) {
		@Override
		public void evaluate(final EncogProgram prg) {
			final int b = (int) prg.getStack().pop().toFloatValue();
			final double a = prg.getStack().pop().toFloatValue();

			prg.getStack().push(prg.getContext().getFormat().format(a, b));
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard string left function.
	 */
	public static ProgramExtensionTemplate EXTENSION_LEFT = new BasicTemplate(
			OPCODE_LEFT) {
		@Override
		public void evaluate(final EncogProgram prg) {
			final String str = prg.getStack().pop().toStringValue();
			final int idx = (int) prg.getStack().pop().toFloatValue();
			final String result = str.substring(0, idx);

			prg.getStack().push(result);
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard string right function.
	 */
	public static ProgramExtensionTemplate EXTENSION_RIGHT = new BasicTemplate(
			OPCODE_RIGHT) {
		@Override
		public void evaluate(final EncogProgram prg) {
			final String str = prg.getStack().pop().toStringValue();
			final int idx = (int) prg.getStack().pop().toFloatValue();
			final String result = str.substring(0, idx);

			prg.getStack().push(result);
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard string cint function.
	 */
	public static ProgramExtensionTemplate EXTENSION_CINT = new BasicTemplate(
			OPCODE_CINT) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(prg.getStack().pop().toIntValue());
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard string cfloat function.
	 */
	public static ProgramExtensionTemplate EXTENSION_CFLOAT = new BasicTemplate(
			OPCODE_CFLOAT) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(prg.getStack().pop().toFloatValue());
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard string cstr function.
	 */
	public static ProgramExtensionTemplate EXTENSION_CSTR = new BasicTemplate(
			OPCODE_CSTR) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(prg.getStack().pop().toStringValue());
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard string cbool function.
	 */
	public static ProgramExtensionTemplate EXTENSION_CBOOL = new BasicTemplate(
			OPCODE_CBOOL) {
		@Override
		public void evaluate(final EncogProgram prg) {
			prg.getStack().push(prg.getStack().pop().toBooleanValue());
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard string iff function.
	 */
	public static ProgramExtensionTemplate EXTENSION_IFF = new BasicTemplate(
			OPCODE_IFF) {
		@Override
		public void evaluate(final EncogProgram prg) {
			final boolean a = prg.getStack().pop().toBooleanValue();
			if (a) {
				prg.getStack().pop();
			} else {
				prg.getStack().pop();
			}
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	/**
	 * Standard string clamp function.
	 */
	public static ProgramExtensionTemplate EXTENSION_CLAMP = new BasicTemplate(
			OPCODE_CLAMP) {
		@Override
		public void evaluate(final EncogProgram prg) {
			final double value = prg.getStack().pop().toFloatValue();
			final double min = prg.getStack().pop().toFloatValue();
			final double max = prg.getStack().pop().toFloatValue();
			if (value < min) {
				prg.getStack().push(min);
			} else if (value > max) {
				prg.getStack().push(max);
			} else {
				prg.getStack().push(value);
			}
		}

		@Override
		public int getInstructionSize(final OpCodeHeader header) {
			return 1;
		}
	};

	public static void createAll(final FunctionFactory factory) {
		createNumericOperators(factory);
		createBooleanOperators(factory);
		createTrigFunctions(factory);
		createBasicFunctions(factory);
		createConversionFunctions(factory);
		createStringFunctions(factory);
		createNumericConst(factory);
		factory.addExtension(EXTENSION_TODEG);
		factory.addExtension(EXTENSION_TORAD);
	}

	public static void createBasicFunctions(final FunctionFactory factory) {
		factory.addExtension(EXTENSION_ABS);
		factory.addExtension(EXTENSION_CEIL);
		factory.addExtension(EXTENSION_EXP);
		factory.addExtension(EXTENSION_FLOOR);
		factory.addExtension(EXTENSION_LOG);
		factory.addExtension(EXTENSION_LOG10);
		factory.addExtension(EXTENSION_MAX);
		factory.addExtension(EXTENSION_MIN);
		factory.addExtension(EXTENSION_POWFN);
		factory.addExtension(EXTENSION_RANDOM);
		factory.addExtension(EXTENSION_ROUND);
		factory.addExtension(EXTENSION_CLAMP);
	}

	public static void createBooleanOperators(final FunctionFactory factory) {
		factory.addExtension(EXTENSION_AND);
		factory.addExtension(EXTENSION_OR);
		factory.addExtension(EXTENSION_EQUAL);
		factory.addExtension(EXTENSION_LT);
		factory.addExtension(EXTENSION_GT);
		factory.addExtension(EXTENSION_LTE);
		factory.addExtension(EXTENSION_GTE);
		factory.addExtension(EXTENSION_IFF);
		factory.addExtension(EXTENSION_CONST_BOOL);
	}

	public static void createConversionFunctions(final FunctionFactory factory) {
		factory.addExtension(EXTENSION_CINT);
		factory.addExtension(EXTENSION_CFLOAT);
		factory.addExtension(EXTENSION_CSTR);
		factory.addExtension(EXTENSION_CBOOL);
	}

	public static void createNumericConst(final FunctionFactory factory) {
		factory.defineKnownConst((short) 10000, "E",
				new ExpressionValue(Math.E));
		factory.defineKnownConst((short) 10001, "PI", new ExpressionValue(
				Math.PI));

	}

	public static void createNumericOperators(final FunctionFactory factory) {
		factory.addExtension(EXTENSION_VAR_SUPPORT);
		factory.addExtension(EXTENSION_CONST_FLOAT);
		factory.addExtension(EXTENSION_CONST_INT);
		factory.addExtension(EXTENSION_NEG);
		factory.addExtension(EXTENSION_ADD);
		factory.addExtension(EXTENSION_SUB);
		factory.addExtension(EXTENSION_MUL);
		factory.addExtension(EXTENSION_DIV);
		factory.addExtension(EXTENSION_POWER);
		factory.addExtension(EXTENSION_SQRT);
	}

	public static void createStringFunctions(final FunctionFactory factory) {
		factory.addExtension(EXTENSION_CONST_STRING);
		factory.addExtension(EXTENSION_LENGTH);
		factory.addExtension(EXTENSION_FORMAT);
		factory.addExtension(EXTENSION_LEFT);
		factory.addExtension(EXTENSION_RIGHT);
	}

	public static void createTrigFunctions(final FunctionFactory factory) {
		factory.addExtension(EXTENSION_ACOS);
		factory.addExtension(EXTENSION_ASIN);
		factory.addExtension(EXTENSION_ATAN);
		factory.addExtension(EXTENSION_ATAN2);
		factory.addExtension(EXTENSION_COS);
		factory.addExtension(EXTENSION_COSH);
		factory.addExtension(EXTENSION_SIN);
		factory.addExtension(EXTENSION_SINH);
		factory.addExtension(EXTENSION_TAN);
		factory.addExtension(EXTENSION_TANH);
	}
}
