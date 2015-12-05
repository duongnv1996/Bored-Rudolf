package org.faudroids.loooooading.ui;
/**
 * Created by tony.lxy on 2014/9/5.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;

import org.faudroids.loooooading.R;

/**
 * The CustomSwipeRefreshLayout should be used whenever the user can refresh the
 * contents of a view via a vertical swipe gesture. The activity that
 * instantiates this view should add an OnRefreshListener to be notified
 * whenever the swipe to refresh gesture is completed. And refreshComplete()
 * should be called whenever the refreshing is complete. The CustomSwipeRefreshLayout
 * will notify the listener each and every time the gesture is completed again;
 * Two refresh mode are supported:
 * swipe mode: android.support.v4.widget.SwipeRefreshLayout style with custom refresh head
 * pull mode: pull-to-refresh style with progress bar and custom refresh head
 */
public class CustomSwipeRefreshLayout extends ViewGroup {

    public static final boolean DEBUG = true;
    public static final String TAG = "csrl";

    // time out for no movements during swipe action
    private static final int RETURN_TO_ORIGINAL_POSITION_TIMEOUT = 500;

    // time out for showing refresh complete
    private static final int REFRESH_COMPLETE_POSITION_TIMEOUT = 1000;

    // Duration of the animation from the top of the content view to parent top
    private static final int RETURN_TO_TOP_DURATION = 500;

    // Duration of the animation from the top of the content view to the height of header
    private static final int RETURN_TO_HEADER_DURATION = 500;

    // deceleration of progress bar
    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;

    // maximum swipe distance( percent of parent container)
    private static final float MAX_SWIPE_DISTANCE_FACTOR = .9f;

    // swipe resistance factor
    private static final float RESISTANCE_FACTOR = .9f;


    private final DecelerateInterpolator mDecelerateInterpolator;
    private final Animation mAnimateStayComplete = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            // DO NOTHING
        }
    };
    private State currentState = new State(State.STATE_NORMAL);
    private State lastState = new State(-1);
    private RefreshCheckHandler mRefreshCheckHandler;
    private ScrollUpHandler mScrollUpHandler;
    private ScrollLeftOrRightHandler mScrollLeftOrRightHandler;
    private int mReturnToOriginalTimeout = RETURN_TO_ORIGINAL_POSITION_TIMEOUT;
    private int mRefreshCompleteTimeout = REFRESH_COMPLETE_POSITION_TIMEOUT;
    private float mResistanceFactor = RESISTANCE_FACTOR;
    private int mReturnToTopDuration = RETURN_TO_TOP_DURATION;
    private int mReturnToHeaderDuration = RETURN_TO_HEADER_DURATION;
    private View mHeadview;
    private boolean hasHeadview;
    //the content that gets pulled down
    private View mTarget = null;
    private int mTargetOriginalTop;
	private OnRefreshListener mListener;
    private MotionEvent mDownEvent;
    private int mFrom;
    private boolean mRefreshing = false;
    private int mTouchSlop;
    private int mDistanceToTriggerSync = -1;
    private float mPrevY;
    private boolean enableHorizontalScroll = true;
    private boolean isHorizontalScroll;
    private boolean checkHorizontalMove;
    private boolean mCheckValidMotionFlag = true;
    private int mCurrentTargetOffsetTop = 0;
    private final AnimationListener mReturningAnimationListener = new BaseAnimationListener() {
        @Override
        public void onAnimationEnd(Animation animation) {
            // Once the target content has returned to its start position, reset
            // the target offset to 0
            // mCurrentTargetOffsetTop = 0;
            mInReturningAnimation = false;
        }
    };

    private boolean mInReturningAnimation;
    private int mTriggerOffset = 0;

    private final Runnable mReturnToTriggerPosition = new Runnable() {

        @Override
        public void run() {
            mInReturningAnimation = true;
            animateOffsetToTriggerPosition(mTarget.getTop(),
					mReturningAnimationListener);
        }

    };

    private final Runnable mReturnToStartPosition = new Runnable() {

        @Override
        public void run() {
            mInReturningAnimation = true;
            animateOffsetToStartPosition(mTarget.getTop(),
                    mReturningAnimationListener);
        }

    };

    private final AnimationListener mStayCompleteListener = new BaseAnimationListener() {
        @Override
        public void onAnimationEnd(Animation animation) {
            mReturnToStartPosition.run();
            mRefreshing = false;
        }
    };

    private final Runnable mStayRefreshCompletePosition = new Runnable() {

        @Override
        public void run() {
            animateStayComplete(mStayCompleteListener);
        }

    };

    // Cancel the refresh gesture and animate everything back to its original state.
    private final Runnable mCancel = new Runnable() {
        @Override
        public void run() {
            mInReturningAnimation = true;
            animateOffsetToStartPosition(mTarget.getTop(), mReturningAnimationListener);
        }
    };

    private final Animation mAnimateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop = mTargetOriginalTop;
            if (mFrom != mTargetOriginalTop) {
                targetTop = (mFrom + (int) ((mTargetOriginalTop - mFrom) * interpolatedTime));
            }
            int offset = targetTop - mTarget.getTop();
            final int currentTop = mTarget.getTop();
            if (offset + currentTop < 0) {
                offset = 0 - currentTop;
            }
            setTargetOffsetTop(offset, true);
        }
    };

    private final Animation mAnimateToTriggerPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop = mDistanceToTriggerSync;
            if (mFrom > mDistanceToTriggerSync) {
                targetTop = (mFrom + (int) ((mDistanceToTriggerSync - mFrom) * interpolatedTime));
            }
            int offset = targetTop - mTarget.getTop();
            final int currentTop = mTarget.getTop();
            if (offset + currentTop < 0) {
                offset = 0 - currentTop;
            }
            setTargetOffsetTop(offset, true);
        }
    };


    /**
     * Simple constructor to use when creating a CustomSwipeRefreshLayout from code.
     *
     * @param context
     */
    public CustomSwipeRefreshLayout(Context context) {
        this(context, null);
    }

    /**
     * Constructor that is called when inflating CustomSwipeRefreshLayout from XML.
     *
     * @param context
     * @param attrs
     */
    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setWillNotDraw(false);

        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomSwipeRefreshLayout);
        if (a != null) {
            mReturnToOriginalTimeout = a.getInteger(R.styleable.CustomSwipeRefreshLayout_time_out_return_to_top, RETURN_TO_ORIGINAL_POSITION_TIMEOUT);
            mRefreshCompleteTimeout = a.getInteger(R.styleable.CustomSwipeRefreshLayout_time_out_refresh_complete, REFRESH_COMPLETE_POSITION_TIMEOUT);
            mReturnToTopDuration = a.getInteger(R.styleable.CustomSwipeRefreshLayout_return_to_top_duration, RETURN_TO_TOP_DURATION);
            mReturnToHeaderDuration = a.getInteger(R.styleable.CustomSwipeRefreshLayout_return_to_header_duration, RETURN_TO_HEADER_DURATION);
            a.recycle();
        }

		mTriggerOffset = getResources().getDimensionPixelOffset(R.dimen.game_height);
    }

    private void animateStayComplete(AnimationListener listener) {
        mAnimateStayComplete.reset();
        mAnimateStayComplete.setDuration(mRefreshCompleteTimeout);
        mAnimateStayComplete.setAnimationListener(listener);
        mTarget.startAnimation(mAnimateStayComplete);
    }

    private void animateOffsetToTriggerPosition(int from, AnimationListener listener) {
        mFrom = from;
        mAnimateToTriggerPosition.reset();
        mAnimateToTriggerPosition.setDuration(mReturnToHeaderDuration);
        mAnimateToTriggerPosition.setAnimationListener(listener);
        mAnimateToTriggerPosition.setInterpolator(mDecelerateInterpolator);
        mTarget.startAnimation(mAnimateToTriggerPosition);
    }

    private void animateOffsetToStartPosition(int from, AnimationListener listener) {
        mFrom = from;
        mAnimateToStartPosition.reset();
        mAnimateToStartPosition.setDuration(mReturnToTopDuration);
        mAnimateToStartPosition.setAnimationListener(listener);
        mAnimateToStartPosition.setInterpolator(mDecelerateInterpolator);
        mTarget.startAnimation(mAnimateToStartPosition);
    }


    /**
     * @return Whether it is possible for the child view of this layout to
     * scroll up. Override this if the child view is a custom view.
     */
    private boolean canViewScrollUp(View view, MotionEvent event) {
        boolean ret;

        event.offsetLocation(view.getScrollX() - view.getLeft(), view.getScrollY() - view.getTop());
        if (mScrollUpHandler != null) {
            boolean canViewScrollUp = mScrollUpHandler.canScrollUp(view);
            if (canViewScrollUp)
                return true;
        }

        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (view instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) view;
                ret = absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                ret = view.getScrollY() > 0 || canChildrenScrollUp(view, event);
            }
        } else {
            ret = ViewCompat.canScrollVertically(view, -1) || canChildrenScrollUp(view, event);
        }
        if (DEBUG)
            Log.d(TAG, "canViewScrollUp " + view.getClass().getName() + " " + ret);
        return ret;
    }

    private boolean canChildrenScrollUp(View view, MotionEvent event) {
        if (view instanceof ViewGroup) {
            final ViewGroup viewgroup = (ViewGroup) view;
            int count = viewgroup.getChildCount();
            for (int i = 0; i < count; ++i) {
                View child = viewgroup.getChildAt(i);
                Rect bounds = new Rect();
                child.getHitRect(bounds);
                if (bounds.contains((int) event.getX(), (int) event.getY())) {
                    return canViewScrollUp(child, event);
                }
            }
        }

        return false;
    }

    /**
     * @param direction Negative to check scrolling left, positive to check scrolling right.
     * @return Whether it is possible for the child view of this layout to
     * scroll left or right. Override this if the child view is a custom view.
     */
    private boolean canViewScrollHorizontally(View view, MotionEvent event, int direction) {
        boolean ret;
        event.offsetLocation(view.getScrollX() - view.getLeft(), view.getScrollY() - view.getTop());
        if (mScrollLeftOrRightHandler != null) {
            boolean canViewScrollLeftOrRight = mScrollLeftOrRightHandler.canScrollLeftOrRight(view, direction);
            if (canViewScrollLeftOrRight)
                return true;
        }
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (view instanceof ViewPager) {
                ret = ((ViewPager) view).canScrollHorizontally(direction);
            } else {
                ret = view.getScrollX() * direction > 0;
            }
        } else {
            ret = ViewCompat.canScrollHorizontally(view, direction);
        }

        ret = ret || canChildrenScrollHorizontally(view, event, direction);
        if (DEBUG)
            Log.d(TAG, "canViewScrollHorizontally " + view.getClass().getName() + " " + ret);
        return ret;
    }

    private boolean canChildrenScrollHorizontally(View view, MotionEvent event, int direction) {
        if (view instanceof ViewGroup) {
            final ViewGroup viewgroup = (ViewGroup) view;
            int count = viewgroup.getChildCount();
            for (int i = 0; i < count; ++i) {
                View child = viewgroup.getChildAt(i);
                Rect bounds = new Rect();
                child.getHitRect(bounds);
                if (bounds.contains((int) event.getX(), (int) event.getY())) {
                    if (DEBUG)
                        Log.d(TAG, "in child " + child.getClass().getName());
                    return canViewScrollHorizontally(child, event, direction);
                }
            }
        }
        return false;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        removeCallbacks(mCancel);
        removeCallbacks(mReturnToStartPosition);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeCallbacks(mReturnToStartPosition);
        removeCallbacks(mCancel);
    }


    /**
     * Set the listener to be notified when a refresh is triggered via the swipe
     * gesture.
     */
    public void setOnRefreshListener(OnRefreshListener listener) {
        mListener = listener;
    }

    // for headview
    private void setRefreshState(int state) {
        currentState.update(state, mCurrentTargetOffsetTop, mTriggerOffset);
        ((CustomSwipeRefreshHeadLayout) mHeadview).onStateChange(currentState, lastState);
        lastState.update(state, mCurrentTargetOffsetTop, mTriggerOffset);
    }

    private void updateHeadViewState(boolean changeHeightOnly) {
        if (changeHeightOnly) {
            setRefreshState(currentState.getRefreshState());
        } else {
            if (mTarget.getTop() > mDistanceToTriggerSync) {
                setRefreshState(State.STATE_READY);
            } else {
                setRefreshState(State.STATE_NORMAL);
            }
        }
    }

    public void refreshComplete() {
        setRefreshing(false);
    }

    /**
     * @return Whether the SwipeRefreshWidget is actively showing refresh
     * progress.
     */
    public boolean isRefreshing() {
        return mRefreshing;
    }

    /**
     * Notify the widget that refresh state has changed. Do not call this when
     * refresh is triggered by a swipe gesture.
     *
     * @param refreshing Whether or not the view should show refresh progress.
     */
    protected void setRefreshing(boolean refreshing) {
        if (mRefreshing != refreshing) {
            ensureTarget();
            mRefreshing = refreshing;
			if (mRefreshing) {
				mReturnToTriggerPosition.run();

            } else {
                // keep refreshing state for refresh complete
				mRefreshing = true;
				removeCallbacks(mReturnToStartPosition);
				removeCallbacks(mCancel);
				mStayRefreshCompletePosition.run();
                setRefreshState(State.STATE_COMPLETE);
            }
        }
    }

    private View getContentView() {
        return getChildAt(0) == mHeadview ? getChildAt(1) : getChildAt(0);
    }

    private void ensureTarget() {
        // Don't bother getting the parent height if the parent hasn't been laid out yet.
        if (mTarget == null) {
            if (getChildCount() > 2 && !isInEditMode()) {
                throw new IllegalStateException(
                        "CustomSwipeRefreshLayout can host ONLY one direct child");
            }
            mTarget = getContentView();
            mTargetOriginalTop = mTarget.getTop();
			int mOriginalOffsetBottom = mTargetOriginalTop + mTarget.getHeight();

            if (DEBUG) {
                Log.d(TAG, "mTargetOriginalTop = " + mTargetOriginalTop +
                        ", mOriginalOffsetBottom = " + mOriginalOffsetBottom);
            }
        }
        if (mDistanceToTriggerSync == -1) {
            if (getParent() != null && ((View) getParent()).getHeight() > 0) {
                mDistanceToTriggerSync = (int) Math.min(
                        ((View) getParent()).getHeight() * MAX_SWIPE_DISTANCE_FACTOR,
                        mTriggerOffset + mTargetOriginalTop);

            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (getChildCount() == 0) {
            return;
        }
        MarginLayoutParams lp = (MarginLayoutParams) mHeadview.getLayoutParams();
        final int headViewLeft = getPaddingLeft() + lp.leftMargin;
        final int headViewTop = mCurrentTargetOffsetTop - mHeadview.getMeasuredHeight() +
                getPaddingTop() + lp.topMargin;
        final int headViewRight = headViewLeft + mHeadview.getMeasuredWidth();
        final int headViewBottom = headViewTop + mHeadview.getMeasuredHeight();
        mHeadview.layout(headViewLeft, headViewTop, headViewRight, headViewBottom);
        if (DEBUG)
            Log.d(TAG, String.format("@@ onLayout() : mHeadview [%d,%d,%d,%d] ",
                    headViewLeft, headViewTop, headViewRight, headViewBottom));

        final View content = getContentView();
        lp = (MarginLayoutParams) content.getLayoutParams();
        final int childLeft = getPaddingLeft() + lp.leftMargin;
        final int childTop = mCurrentTargetOffsetTop + getPaddingTop() + lp.topMargin;
        final int childRight = childLeft + content.getMeasuredWidth();
        final int childBottom = childTop + content.getMeasuredHeight();
        content.layout(childLeft, childTop, childRight, childBottom);
        if (DEBUG)
            Log.d(TAG, String.format("@@ onLayout() %d : content [%d,%d,%d,%d] ",
                    getChildAt(0) == mHeadview ? 1 : 0, childLeft, childTop, childRight, childBottom));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!hasHeadview) {
			mHeadview = new GameView(getContext());
			addView(mHeadview, new MarginLayoutParams(LayoutParams.MATCH_PARENT, getResources().getDimensionPixelOffset(R.dimen.game_height)));
			hasHeadview = true;
        }

        if (getChildCount() > 2 && !isInEditMode()) {
            throw new IllegalStateException("CustomSwipeRefreshLayout can host one child content view.");
        }

        measureChildWithMargins(mHeadview, widthMeasureSpec, 0, heightMeasureSpec, 0);

        final View content = getContentView();
        if (getChildCount() > 0) {
            MarginLayoutParams lp = (MarginLayoutParams) content.getLayoutParams();
            content.measure(
                    MeasureSpec.makeMeasureSpec(
                            getMeasuredWidth() - getPaddingLeft() - getPaddingRight() -
                                    lp.leftMargin - lp.rightMargin,
                            MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(
                            getMeasuredHeight() - getPaddingTop() - getPaddingBottom() -
                                    lp.topMargin - lp.bottomMargin,
                            MeasureSpec.EXACTLY));
        }

        if (DEBUG) {
            Log.d(TAG, String.format("onMeasure(): swiperefreshlayout: width=%d, height=%d",
                    getMeasuredWidth(), getMeasuredHeight()));
            Log.d(TAG, String.format("onMeasure(): headview: width=%d, height=%d",
                    mHeadview.getMeasuredWidth(), mHeadview.getMeasuredHeight()));
            Log.d(TAG, String.format("onMeasure(): content: width=%d, height=%d",
                    content.getMeasuredWidth(), content.getMeasuredHeight()));
        }
    }


    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }


    @Override
    public void addView(View child, int index, LayoutParams params) {
        if (getChildCount() > 1 && !isInEditMode()) {
            throw new IllegalStateException("CustomSwipeRefreshLayout can host ONLY one child content view");
        }
        super.addView(child, index, params);
    }

    private boolean checkCanDoRefresh() {
        if (mRefreshCheckHandler != null) {
            return mRefreshCheckHandler.canRefresh();
        }
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (DEBUG)
            Log.d(TAG, "dispatchTouchEvent() start ");
        boolean ret = super.dispatchTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            ret = true;
        if (DEBUG)
            Log.d(TAG, "dispatchTouchEvent() " + ret);
        return ret;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (DEBUG)
            Log.d(TAG, "onInterceptTouchEvent() start " + ev);
        ensureTarget();
        boolean handled = false;
        float curY = ev.getY();

        if (!isEnabled()) {
            return false;
        }

        // record the first event:
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mDownEvent = MotionEvent.obtain(ev);
            mPrevY = mDownEvent.getY();
            mCheckValidMotionFlag = true;
            checkHorizontalMove = true;
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            float yDiff = Math.abs(curY - mDownEvent.getY());

            if (enableHorizontalScroll) {

                MotionEvent event = MotionEvent.obtain(ev);
                int horizontalScrollDirection = ev.getX() > mDownEvent.getX() ? -1 : 1;
                float xDiff = Math.abs(ev.getX() - mDownEvent.getX());
                if (isHorizontalScroll) {
                    if (DEBUG)
                        Log.d(TAG, "onInterceptTouchEvent(): in horizontal scroll");
                    mPrevY = curY;
                    checkHorizontalMove = false;
                    return false;
                } else if (xDiff <= mTouchSlop) {
                    checkHorizontalMove = true;
                    //return false;
                } else if (canViewScrollHorizontally(mTarget, event, horizontalScrollDirection) &&
                        checkHorizontalMove && xDiff > 2 * yDiff) {
                    if (DEBUG)
                        Log.d(TAG, "onInterceptTouchEvent(): start horizontal scroll");
                    mPrevY = curY;
                    isHorizontalScroll = true;
                    checkHorizontalMove = false;
                    return false;
                } else {
                    checkHorizontalMove = false;
                }
            }

            if (yDiff < mTouchSlop) {
                mPrevY = curY;
                return false;
            }
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            float yDiff = Math.abs(curY - mDownEvent.getY());
            if (enableHorizontalScroll && isHorizontalScroll) {
                if (DEBUG)
                    Log.d(TAG, "onInterceptTouchEvent(): finish horizontal scroll");
                isHorizontalScroll = false;
                mPrevY = ev.getY();
                return false;
            } else if (yDiff < mTouchSlop) {
                mPrevY = curY;
                return false;
            }
        }


        MotionEvent event = MotionEvent.obtain(ev);
        if (!mInReturningAnimation && !canViewScrollUp(mTarget, event)) {
            handled = onTouchEvent(ev);
            if (DEBUG)
                Log.d(TAG, "onInterceptTouchEvent(): handled = onTouchEvent(event);" + handled);
        } else {
            // keep updating last Y position when the event is not intercepted!
            mPrevY = ev.getY();
        }

        boolean ret = !handled ? super.onInterceptTouchEvent(ev) : handled;
        if (DEBUG)
            Log.d(TAG, "onInterceptTouchEvent() " + ret);
        return ret;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        // Nope.
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (DEBUG)
            Log.d(TAG, "onTouchEvent() start");

        if (!isEnabled()) {
            return false;
        }

        final int action = event.getAction();
        boolean handled = false;
        int curTargetTop = mTarget.getTop();
        mCurrentTargetOffsetTop = curTargetTop - mTargetOriginalTop;
        switch (action) {

            case MotionEvent.ACTION_MOVE:
                if (mDownEvent != null && !mInReturningAnimation) {
                    final float eventY = event.getY();
                    float yDiff = eventY - mDownEvent.getY();


                    boolean isScrollUp = eventY - mPrevY > 0;

                    // if yDiff is large enough to be counted as one move event
                    if (mCheckValidMotionFlag && (yDiff > mTouchSlop || yDiff < -mTouchSlop)) {
                        mCheckValidMotionFlag = false;
                    }
                    // keep refresh head above mTarget when refreshing
					if (isRefreshing()) {
						mPrevY = event.getY();
						handled = false;
						break;
					}

                    // curTargetTop is bigger than trigger
                    if (curTargetTop >= mDistanceToTriggerSync) {
                        // User movement passed distance; trigger a refresh
                        removeCallbacks(mCancel);
                    }
                    // curTargetTop is not bigger than trigger
                    else {
                        // Just track the user's movement
                        if (!isScrollUp && (curTargetTop < mTargetOriginalTop + 1)) {
                            removeCallbacks(mCancel);
                            mPrevY = event.getY();
                            handled = false;
                            break;
                        } else {
                            updatePositionTimeout(true);
                        }

                    }

                    handled = true;
                    if (curTargetTop >= mTargetOriginalTop && !isRefreshing())
                        setTargetOffsetTop((int) ((eventY - mPrevY) * mResistanceFactor), false);
                    else
                        setTargetOffsetTop((int) ((eventY - mPrevY)), true);
                    mPrevY = event.getY();
                }

                break;
            case MotionEvent.ACTION_UP:
                if (mRefreshing) break;
				if (mCurrentTargetOffsetTop >= mTriggerOffset) {
					startRefresh();
				} else {
					updatePositionTimeout(false);
				}
				handled = true;
				break;

            case MotionEvent.ACTION_CANCEL:
                if (mDownEvent != null) {
                    mDownEvent.recycle();
                    mDownEvent = null;
                }
                break;
        }

        if (DEBUG)
            Log.d(TAG, "onTouchEvent() " + handled);
        return handled;
    }

    private void startRefresh() {
        if (!checkCanDoRefresh()) {
            updatePositionTimeout(false);
            return;
        }
        removeCallbacks(mCancel);
        setRefreshState(State.STATE_REFRESHING);
        setRefreshing(true);
        if (mListener != null)
            mListener.onRefresh();
    }

    private void updateContentOffsetTop(int targetTop, boolean changeHeightOnly) {
        final int currentTop = mTarget.getTop();
        if (targetTop < mTargetOriginalTop) {
            targetTop = mTargetOriginalTop;
        }
        setTargetOffsetTop(targetTop - currentTop, changeHeightOnly);
    }


    private void setTargetOffsetTop(int offset, boolean changeHeightOnly) {
        if (offset == 0)
            return;
        // check whether the mTarget total top offset is going to be smaller than 0
        if (mCurrentTargetOffsetTop + offset >= 0) {
            mTarget.offsetTopAndBottom(offset);
            mHeadview.offsetTopAndBottom(offset);
            mCurrentTargetOffsetTop += offset;
            invalidate();
        } else {
            updateContentOffsetTop(mTargetOriginalTop, changeHeightOnly);
        }
        updateHeadViewState(changeHeightOnly);
    }

    private void updatePositionTimeout(boolean isDelayed) {
        removeCallbacks(mCancel);
        if (isDelayed && mReturnToOriginalTimeout <= 0)
            return;
        postDelayed(mCancel, isDelayed ? mReturnToOriginalTimeout : 0);
    }


    public void setEnableHorizontalScroll(boolean isEnable) {
        enableHorizontalScroll = isEnable;
    }

    public void setReturnToOriginalTimeout(int mReturnToOriginalTimeout) {
        this.mReturnToOriginalTimeout = mReturnToOriginalTimeout;
    }

    public int getReturnToOriginalTimeout() {
        return this.mReturnToOriginalTimeout;
    }

    public int getRefreshCompleteTimeout() {
        return mRefreshCompleteTimeout;
    }

    public void setRefreshCompleteTimeout(int mRefreshCompleteTimeout) {
        this.mRefreshCompleteTimeout = mRefreshCompleteTimeout;
    }

    public void setReturnToTopDuration(int duration) {
        this.mReturnToTopDuration = duration;
    }

    public int getReturnToTopDuration() {
        return this.mReturnToTopDuration;
    }

    public void setReturnToHeaderDuration(int duration) {
        this.mReturnToHeaderDuration = duration;
    }

    public int getReturnToHeaderDuration() {
        return this.mReturnToHeaderDuration;
    }

    public void setRefreshCheckHandler(RefreshCheckHandler handler) {
        mRefreshCheckHandler = handler;
    }

    public void setScroolUpHandler(ScrollUpHandler handler) {
        mScrollUpHandler = handler;
    }

    public void setScroolLeftOrRightHandler(ScrollLeftOrRightHandler handler) {
        mScrollLeftOrRightHandler = handler;
    }

    public float getResistanceFactor() {
        return mResistanceFactor;
    }

    public void setResistanceFactor(float factor) {
        mResistanceFactor = factor;
    }

    /**
     * Classes that wish to be notified when the swipe gesture correctly
     * triggers a refresh should implement this interface.
     */
    public interface OnRefreshListener {
        void onRefresh();
    }

    /**
     * Classes that checking whether refresh can be triggered
     */
    public interface RefreshCheckHandler {
        boolean canRefresh();
    }

    public interface ScrollUpHandler {
        boolean canScrollUp(View view);
    }

    public interface ScrollLeftOrRightHandler {
        boolean canScrollLeftOrRight(View view, int direction);
    }

    /**
     * Classes that must be implemented by for custom headview
     */
    public interface CustomSwipeRefreshHeadLayout {
        void onStateChange(State currentState, State lastState);
    }

    /**
     * Refresh state
     */
    public static class State {
        public final static int STATE_NORMAL = 0;
        public final static int STATE_READY = 1;
        public final static int STATE_REFRESHING = 2;
        public final static int STATE_COMPLETE = 3;

        /**
         * detailed refresh state code
         */
        private int refreshState = STATE_NORMAL;

        /**
         * scroll distance relative to refresh trigger distance
         * percent = top / trigger;
         */
        private float percent;

        /**
         * distance from header top to parent top.
         */
        private int headerTop;

        /**
         * distance from header top to parent top to trigger refresh
         */
        private int trigger;

        public State(int refreshState) {
            this.refreshState = refreshState;
        }

        void update(int refreshState, int top, int trigger) {
            this.refreshState = refreshState;
            this.headerTop = top;
            this.trigger = trigger;
            this.percent = (float) top / trigger;
        }

        public int getRefreshState() {
            return refreshState;
        }

    }

    /**
     * Simple AnimationListener to avoid having to implement unneeded methods in
     * AnimationListeners.
     */
    private class BaseAnimationListener implements AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }
}
