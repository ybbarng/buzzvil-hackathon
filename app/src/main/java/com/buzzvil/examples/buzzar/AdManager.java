package com.buzzvil.examples.buzzar;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.buzzvil.buzzad.benefit.BuzzAdBenefit;
import com.buzzvil.buzzad.benefit.BuzzAdBenefitConfig;
import com.buzzvil.buzzad.benefit.core.ad.AdError;
import com.buzzvil.buzzad.benefit.core.models.Ad;
import com.buzzvil.buzzad.benefit.core.models.Creative;
import com.buzzvil.buzzad.benefit.core.models.UserProfile;
import com.buzzvil.buzzad.benefit.presentation.media.CtaPresenter;
import com.buzzvil.buzzad.benefit.presentation.media.CtaView;
import com.buzzvil.buzzad.benefit.presentation.media.MediaView;
import com.buzzvil.buzzad.benefit.presentation.nativead.NativeAd;
import com.buzzvil.buzzad.benefit.presentation.nativead.NativeAdLoader;
import com.buzzvil.buzzad.benefit.presentation.nativead.NativeAdRewardResult;
import com.buzzvil.buzzad.benefit.presentation.nativead.NativeAdView;
import com.buzzvil.buzzad.benefit.presentation.video.VideoErrorStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AdManager {
    private static String appId;
    private static String adUnitId;

    public static void initBuzzAdBenefit(final Context applicationContext, final String appId, final String adUnitId) {
        AdManager.appId = appId;
        AdManager.adUnitId = adUnitId;
        final BuzzAdBenefitConfig buzzAdBenefitConfig = new BuzzAdBenefitConfig.Builder(appId)
                .build();
        BuzzAdBenefit.init(applicationContext, buzzAdBenefitConfig);

        final UserProfile userProfile = new UserProfile.Builder(BuzzAdBenefit.getUserProfile())
                .userId("SAMPLE_USER_ID")
                .gender(UserProfile.Gender.FEMALE)
                .birthYear(1993)
                .build();
        BuzzAdBenefit.setUserProfile(userProfile);
    }

    public static CompletableFuture<NativeAd> loadAd(final Context activityContext) {
        Log.d("MYAR", "Try loadAd");
        final NativeAdLoader loader = new NativeAdLoader(adUnitId);
        final CompletableFuture<NativeAd> result = new CompletableFuture<>();
        loader.loadAd(new NativeAdLoader.OnAdLoadedListener() {
            @Override
            public void onLoadError(@NonNull AdError error) {
                result.completeExceptionally(error);
            }

            @Override
            public void onAdLoaded(@NonNull NativeAd nativeAd) {
                Log.d("MYAR", "Ad loaded");
                result.complete(nativeAd);
            }
        });
        return result;
    }

    public static View populateAd_old(final Context activityContext, final NativeAd nativeAd) {
        Log.d("MYAR", "populateAd");
        final Ad ad = nativeAd.getAd();
        final Creative.Type creativeType = ad.getCreative() == null ? null : ad.getCreative().getType();

        final View interstitialView = LayoutInflater.from(activityContext).inflate(R.layout.bz_fragment_interstitial_ad, null, false);
        final TextView cardTitleTextView = interstitialView.findViewById(R.id.card_title_text);
        final TextView closeTextView = interstitialView.findViewById(R.id.ad_close_text);
        final NativeAdView nativeAdView = interstitialView.findViewById(R.id.native_ad_view);
        final MediaView mediaView = interstitialView.findViewById(R.id.ad_media_view);
        final TextView titleTextView = interstitialView.findViewById(R.id.ad_title_text);
        final TextView descriptionTextView = interstitialView.findViewById(R.id.ad_description_text);
        final ImageView iconImageView = interstitialView.findViewById(R.id.ad_icon_image);
        final CtaView ctaView = interstitialView.findViewById(R.id.ad_cta_view);

        cardTitleTextView.setText(R.string.bz_interstitial_title);
        cardTitleTextView.setTextSize(14);
        closeTextView.setText(R.string.bz_interstitial_close);
        closeTextView.setVisibility(View.VISIBLE);

        mediaView.setCreative(ad.getCreative());
        titleTextView.setText(ad.getTitle());
        descriptionTextView.setText(ad.getDescription());
        Glide.with(activityContext).load(ad.getIconUrl()).into(iconImageView);
        final CtaPresenter ctaPresenter = new CtaPresenter(ctaView);
        ctaPresenter.bind(nativeAd);

        final List<View> clickableViews = new ArrayList<>();
        clickableViews.add(ctaView);
        clickableViews.add(mediaView);

        if (Creative.Type.IMAGE.equals(creativeType)) {
            titleTextView.setVisibility(View.GONE);
            iconImageView.setVisibility(View.GONE);
            descriptionTextView.setVisibility(View.GONE);
        } else {
            titleTextView.setVisibility(View.VISIBLE);
            iconImageView.setVisibility(View.VISIBLE);
            descriptionTextView.setVisibility(View.VISIBLE);
            clickableViews.add(titleTextView);
            clickableViews.add(iconImageView);
            clickableViews.add(descriptionTextView);
        }

        nativeAdView.setMediaView(mediaView);
        nativeAdView.setClickableViews(clickableViews);
        nativeAdView.setNativeAd(nativeAd);

        mediaView.addOnMediaErrorListener((@NonNull MediaView _mediaView, @NonNull VideoErrorStatus errorStatus, @Nullable String errorMessage) -> {
            if (errorMessage != null) {
                Toast.makeText(_mediaView.getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        nativeAdView.addOnNativeAdEventListener(new NativeAdView.OnNativeAdEventListener() {
            @Override
            public void onImpressed(final @NonNull NativeAdView view, final @NonNull NativeAd nativeAd) {
                Toast.makeText(activityContext, "onImpressed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onClicked(@NonNull NativeAdView view, @NonNull NativeAd nativeAd) {
                Toast.makeText(activityContext, "onClicked", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRewardRequested(@NonNull NativeAdView view, @NonNull NativeAd nativeAd) {
                Toast.makeText(activityContext, "onRewardRequested", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRewarded(@NonNull NativeAdView view, @NonNull NativeAd nativeAd, @Nullable NativeAdRewardResult nativeAdRewardResult) {
                Toast.makeText(activityContext, "onRewarded: " + nativeAdRewardResult, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onParticipated(final @NonNull NativeAdView view, final @NonNull NativeAd nativeAd) {
                Toast.makeText(activityContext, "onParticipated", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ctaPresenter.bind(nativeAd);
                    }
                }, 1000);
            }
        });

        final ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(100, 100);
        interstitialView.setLayoutParams(layoutParams);

        return interstitialView;
    }

    public static View populateAd(final Context activityContext, final NativeAd nativeAd) {
        Log.d("MYAR", "populateAd");
        final Ad ad = nativeAd.getAd();
        final Creative.Type creativeType = ad.getCreative() == null ? null : ad.getCreative().getType();

        final View interstitialView = LayoutInflater.from(activityContext).inflate(R.layout.ad_view, null, false);
        final NativeAdView nativeAdView = interstitialView.findViewById(R.id.native_ad_view);
        final TextView titleTextView = interstitialView.findViewById(R.id.title);
        final MediaView mediaView = interstitialView.findViewById(R.id.mediaview);

        mediaView.setCreative(ad.getCreative());
        titleTextView.setText(ad.getTitle());
        final List<View> clickableViews = new ArrayList<>();
        clickableViews.add(titleTextView);
        clickableViews.add(mediaView);

        if (Creative.Type.IMAGE.equals(creativeType)) {
            titleTextView.setVisibility(View.GONE);
        } else {
            titleTextView.setVisibility(View.VISIBLE);
            clickableViews.add(titleTextView);
        }

        nativeAdView.setMediaView(mediaView);
        nativeAdView.setClickableViews(clickableViews);
        nativeAdView.setNativeAd(nativeAd);

        mediaView.addOnMediaErrorListener((@NonNull MediaView _mediaView, @NonNull VideoErrorStatus errorStatus, @Nullable String errorMessage) -> {
            if (errorMessage != null) {
                Toast.makeText(_mediaView.getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        nativeAdView.addOnNativeAdEventListener(new NativeAdView.OnNativeAdEventListener() {
            @Override
            public void onImpressed(final @NonNull NativeAdView view, final @NonNull NativeAd nativeAd) {
                Toast.makeText(activityContext, "onImpressed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onClicked(@NonNull NativeAdView view, @NonNull NativeAd nativeAd) {
                Toast.makeText(activityContext, "onClicked", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRewardRequested(@NonNull NativeAdView view, @NonNull NativeAd nativeAd) {
                Toast.makeText(activityContext, "onRewardRequested", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRewarded(@NonNull NativeAdView view, @NonNull NativeAd nativeAd, @Nullable NativeAdRewardResult nativeAdRewardResult) {
                Toast.makeText(activityContext, "onRewarded: " + nativeAdRewardResult, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onParticipated(final @NonNull NativeAdView view, final @NonNull NativeAd nativeAd) {
                Toast.makeText(activityContext, "onParticipated", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //ctaPresenter.bind(nativeAd);
                    }
                }, 1000);
            }
        });

        return interstitialView;
    }

}
